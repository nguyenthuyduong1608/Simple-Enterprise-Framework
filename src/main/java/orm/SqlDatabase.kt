package orm

import com.squareup.javapoet.*
import generator.Generatable
import org.apache.commons.io.FileUtils
import orm.column.ColumnBuilder
import orm.table.Table
import orm.table.TableBuilder
import java.io.File
import java.sql.DriverManager
import javax.lang.model.element.Modifier
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

class SqlDatabase(val _sqlServer: SqlServer, private val _databaseName: String) : Generatable {
    lateinit var _tableList: MutableList<Table>
    val _jdbcUrl = "${_sqlServer._baseUrl}/$_databaseName"

    init {
        getTableListFromDb()
    }

    private fun getTableListFromDb() {
        _tableList = ArrayList()
        val connection =
            DriverManager.getConnection(_jdbcUrl, _sqlServer._user, _sqlServer._password)

        val metadata = connection.metaData
        val resultSet = metadata.getTables(connection.catalog, null, "%", null)
        while (resultSet.next()) {
            val tableName = resultSet.getString("TABLE_NAME")
            val table = Table(tableName, connection)
            _tableList.add(table)
        }
    }


    override fun generate(rootProject: File) {
        createTableUser()
        getTableListFromDb()

        val sourceFolder = File(rootProject.absolutePath + "\\src\\main\\java")
        for (table in _tableList)
            table.generate(sourceFolder)

        generateBaseDao(sourceFolder)
        generateEntityManagerProvider(sourceFolder)

    }

    private fun createTableUser() {
        val columnUserName =
            ColumnBuilder().setColumnName("username").setClassName("String").setNullable(false).setIsPrimaryKey(true)
                .build()
        val columnPassword = ColumnBuilder().setColumnName("password").setClassName("String").setNullable(false).build()
        val table = TableBuilder().setTableName("users").addColumn(columnUserName)
            .addColumn(columnPassword).build()
        table.addToDatabase(this)
    }

    private fun generateBaseDao(directory: File) {
        val baseDao = File("src\\main\\resources\\dao\\BaseDao.java")
        val dstFile = File(directory.absolutePath + "\\dao\\BaseDao.java")
        FileUtils.copyFile(baseDao, dstFile)
    }

    private fun generateEntityManagerProvider(directory: File) {
        val persistenceFieldBuilder = FieldSpec.builder(ClassName.get(String::class.java), "PERSISTENCE_UNIT_NAME")
        persistenceFieldBuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        persistenceFieldBuilder.initializer("\$S", "persistence")

        val instanceFieldBuilder = FieldSpec.builder(ClassName.get(EntityManagerFactory::class.java), "INSTANCE")
        instanceFieldBuilder.addModifiers(Modifier.PRIVATE, Modifier.STATIC)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE).build()

        val getInstance = MethodSpec.methodBuilder("createEntityManager")
            .returns(EntityManager::class.java)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .beginControlFlow("if (INSTANCE == null)")
            .addStatement("INSTANCE = \$T.createEntityManagerFactory(PERSISTENCE_UNIT_NAME)", Persistence::class.java)
            .endControlFlow()
            .addStatement("return INSTANCE.createEntityManager()")
            .build()


        val typeSpecBuilder = TypeSpec.classBuilder("EntityManagerProvider")
        typeSpecBuilder.addModifiers(Modifier.PUBLIC)
        typeSpecBuilder.addField(persistenceFieldBuilder.build())
        typeSpecBuilder.addField(instanceFieldBuilder.build())
        typeSpecBuilder.addMethod(constructor)
        typeSpecBuilder.addMethod(getInstance)

        JavaFile.builder("dao", typeSpecBuilder.build())
            .build().writeTo(directory)
    }

}

