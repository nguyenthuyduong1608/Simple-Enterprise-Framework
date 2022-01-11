package orm.table

import orm.column.Column


class TableBuilder {
    val _table = Table()

    fun setTableName(tableName: String): TableBuilder {
        _table._tableName = tableName
        return this
    }

    fun setClassName(className: String): TableBuilder {
        _table._className = className
        return this
    }

    fun addColumn(column: Column): TableBuilder {
        _table._columnList.add(column)
        return this
    }

    fun build(): Table = _table
}