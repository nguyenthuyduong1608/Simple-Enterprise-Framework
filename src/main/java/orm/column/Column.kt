package orm.column

import com.mysql.cj.jdbc.result.ResultSetMetaData
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import org.jetbrains.annotations.NotNull
import java.lang.StringBuilder
import javax.lang.model.element.Modifier
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


class Column() {
    lateinit var _className: String
    lateinit var _columnName: String
    var _isAutoIncrement: Boolean = false
    var _isNullable: Boolean = false
    var _isPrimaryKey: Boolean = false
    lateinit var _fieldName: String

    constructor(resultSetMetaData: ResultSetMetaData, column: Int) : this() {
        _className = resultSetMetaData.getColumnClassName(column)
        _columnName = resultSetMetaData.getColumnName(column)
        _isAutoIncrement = resultSetMetaData.isAutoIncrement(column)
        _isNullable = resultSetMetaData.isNullable(column) != 0
        _isPrimaryKey = resultSetMetaData.fields[column - 1].isPrimaryKey
        _fieldName = Character.toLowerCase(_columnName[0]) + _columnName.substring(1)
        _fieldName = _fieldName.replace("_", "")
        _fieldName = _fieldName.replace(" ", "")
    }

    fun getSimpleClassName(): String {
        val tokens = _className.split(".").toList()
        return tokens.last()
    }

    private fun getPackageName(): String {
        val tokens = _className.split(".").toList()
        var packageName = ""
        for (i in 0..tokens.size - 2) {
            if (i != 0)
                packageName += "."
            packageName += tokens[i]
        }
        return packageName
    }

    fun toFieldSpec(): FieldSpec {
        val typeName = ClassName.get(getPackageName(), getSimpleClassName())
        val fieldSpecBuilder = FieldSpec.builder(typeName, _fieldName, Modifier.PRIVATE)
        if (_isPrimaryKey)
            fieldSpecBuilder.addAnnotation(Id::class.java)

        val annotationColumnName = AnnotationSpec.builder(Column::class.java)
            .addMember("name", "\$S", _columnName)
            .build()

        if (!_isNullable)
            fieldSpecBuilder.addAnnotation(AnnotationSpec.builder(NotNull::class.java).build())

        if (_isAutoIncrement && !_isPrimaryKey)
            fieldSpecBuilder.addAnnotation(
                AnnotationSpec.builder(GeneratedValue::class.java)
                    .addMember("strategy", "\$T.\$L", GenerationType::class.java, GenerationType.AUTO.name).build()
            )

        if (_isAutoIncrement && _isPrimaryKey)
            fieldSpecBuilder.addAnnotation(
                AnnotationSpec.builder(GeneratedValue::class.java)
                    .addMember("strategy", "\$T.\$L", GenerationType::class.java, GenerationType.IDENTITY.name).build()
            )

        fieldSpecBuilder.addAnnotation(annotationColumnName)

        return fieldSpecBuilder.build()
    }

    fun createGetterMethod(): MethodSpec {
        val typeName = ClassName.get(getPackageName(), getSimpleClassName())
        val methodName = "get" + Character.toUpperCase(_fieldName[0]) +
                _fieldName.substring(1)
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .returns(typeName)
            .addStatement("return $_fieldName")
            .build()
    }

    fun createSetterMethod(): MethodSpec {
        val typeName = ClassName.get(getPackageName(), getSimpleClassName())
        val methodName = "set" + Character.toUpperCase(_fieldName[0]) +
                _fieldName.substring(1)
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(typeName, _fieldName)
            .addStatement("this.\$N = \$N", _fieldName, _fieldName)
            .build()
    }

    fun toSql(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(_columnName)
        stringBuilder.append(" ${TypeMapping.javaTypeToSqlType(_className)}")
        if (_isAutoIncrement)
            stringBuilder.append(" AUTO_INCREMENT")
        if (_isPrimaryKey)
            stringBuilder.append(" PRIMARY KEY")
        if (!_isNullable)
            stringBuilder.append(" NOT NULL")
        return stringBuilder.toString()
    }
}

class TypeMapping {
    companion object {
        fun javaTypeToSqlType(javaType: String): String {
            return when (javaType) {
                "String" -> "VARCHAR(50)"
                "Integer" -> "INT"
                "Boolean" -> "BIT"
                "Float" -> "FLOAT"
                "Double" -> "DOUBLE"
                "byte[]" -> "BINARY"
                else -> ""
            }
        }
    }
}