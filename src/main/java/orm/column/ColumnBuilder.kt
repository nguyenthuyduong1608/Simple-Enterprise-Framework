package orm.column

class ColumnBuilder {
    private val _column = Column()

    fun setClassName(className: String): ColumnBuilder {
        _column._className = className
        return this
    }

    fun setColumnName(columnName: String): ColumnBuilder {
        _column._columnName = columnName
        return this
    }

    fun setAutoIncrement(autoIncrement: Boolean): ColumnBuilder {
        _column._isAutoIncrement = autoIncrement
        return this
    }

    fun setNullable(isNullable: Boolean): ColumnBuilder {
        _column._isNullable = isNullable
        return this
    }

    fun setIsPrimaryKey(isPrimaryKey: Boolean): ColumnBuilder {
        _column._isPrimaryKey = isPrimaryKey
        return this
    }

    fun setFieldName(fieldName: String): ColumnBuilder {
        _column._fieldName = fieldName
        return this
    }

    fun build(): Column {
        return _column
    }
}