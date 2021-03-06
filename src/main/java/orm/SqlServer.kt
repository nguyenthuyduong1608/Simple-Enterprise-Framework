package orm

import java.sql.*


class SqlServer(val _user: String, val _password: String, val _baseUrl: String) {
    companion object {
        const val _className = "com.mysql.cj.jdbc.Driver"
    }

    fun connectToServer(): List<String> {
        val databases: MutableList<String> = ArrayList()

        try {
            Class.forName(_className)
            val connection: Connection = DriverManager.getConnection(_baseUrl, _user, _password)
            val metadata: DatabaseMetaData = connection.metaData
            val resultSet: ResultSet = metadata.catalogs
            while (resultSet.next()) {
                val dbName = resultSet.getString("TABLE_CAT")
                databases.add(dbName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return databases
    }

    fun connectToDatabase(schema: String): SqlDatabase {
        return SqlDatabase(this, schema)
    }

}