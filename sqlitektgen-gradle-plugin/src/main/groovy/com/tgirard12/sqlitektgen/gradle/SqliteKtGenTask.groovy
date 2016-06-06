package com.tgirard12.sqlitektgen.gradle

import com.tgirard12.sqlitektgen.gradle.Table.Column
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class SqliteKtGenTask extends DefaultTask {

    SqliteKtGenExtension extension
    DatabaseFileParser databaseFileParser
    KotlinClassGenerator kotlinClassGenerator

    public SqliteKtGenTask() {

        databaseFileParser = new DatabaseFileParser()
        kotlinClassGenerator = new KotlinClassGenerator()
    }

    @TaskAction
    def sqlitektenTask() {

        extension = project.extensions.getByType(SqliteKtGenExtension.class)
        checkExtensionParam()

        def tables = databaseFileParser.parseDatabaseFile(extension.databaseFile)
        tables.forEach {
            def clazz = kotlinClassGenerator.getKotlinClass(it)
            def file = new File(extension.outputDir, it.ktClass + ".kt")
            file.text = clazz
        }
    }

    def checkExtensionParam() {
        if (extension.databaseFile == null)
            throw new SqliteKtGenException("databaseFile not set in 'sqlitektgen' configuration task")
        if (extension.outputDir == null)
            throw new SqliteKtGenException("outputDir not set in 'sqlitektgen' configuration task")
    }

    /**
     * Json database file parser
     */
    class DatabaseFileParser {

        List<Table> parseDatabaseFile(String databaseFilePath) {
            def file = new File(databaseFilePath)
            if (!file.exists())
                throw new SqliteKtGenException("databaseFile ${file.absolutePath} not found")

            return parseJsonContent(file.text)
        }

        List<Table> parseJsonContent(String jsonContent) {
            def json = new JsonSlurper().parseText(jsonContent)
            List<Table> tables = []

            json.eachWithIndex { tab, index ->
                if (tab.table == null)
                    throw new SqliteKtGenException("'table' field required")
                if (tab.ktPackage == null)
                    throw new SqliteKtGenException("'ktPackage' field required")
                if (tab.columns == null || tab.columns.size() < 1)
                    throw new SqliteKtGenException("'columns' object must have at least one field")

                def table = new Table()
                tables << table
                table.name = tab.table
                table.ktClass = tab.ktClass ?: tab.table
                table.ktPackage = tab.ktPackage

                tab.columns.eachWithIndex { col, colIndex ->
                    if (col.name == null)
                        throw new SqliteKtGenException("'columns.name' field required")

                    def column = new Table.Column()
                    table.columns << column
                    column.name = col.name
                    column.ktField = col.ktField ?: col.name
                    column.ktType = col.ktType ?: 'String?'
                    column.nullable = column.ktType.contains('?')
                    column.typeAppend = col.typeAppend ?: ""
                    column.defaultValue = col.defaultValue

                    if (col.insertOrUpdate == null)
                        column.insertOrUpdate = true
                    else
                        column.insertOrUpdate = col.insertOrUpdate
                    if (col.select == null)
                        column.select = true
                    else
                        column.select = col.select
                }
                table.queries = tab.queries ?: [] as HashMap
            }
            return tables
        }
    }

    class KotlinClassGenerator {

        String getKotlinClass(Table table) {
            """
package ${table.ktPackage}

import android.content.ContentValues
import android.database.Cursor

data class ${table.ktClass} (
${getFields(table.columns)}) {

${getCursorConstructor(table.columns)}

    companion object {
        const val TABLE_NAME = "${table.name}"
${getConstColumnName(table.columns)}
${getCreateTableQuery(table)}
${getConstQueries(table.queries)}\
    }

${getContentValue(table.columns)}
}
""".replaceAll('\t', '    ')
        }

        def getContentValue(List<Column> columns) {
            def strb = new StringBuilder(
                    """\
    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
""")
            columns.forEach {
                if (!it.insertOrUpdate)
                    return

                if (it.nullable) {
                    strb.append """\
            if ($it.ktField == null) cv.putNull(${it.nameUpper()}) else cv.put(${it.nameUpper()}, $it.ktField)\n"""
                } else {
                    strb.append """\
            cv.put(${it.nameUpper()}, $it.ktField)\n"""
                }
            }
            strb.append """\
            return cv
        }"""
        }

        def getFields(List<Column> columns) {
            def strb = new StringBuilder()
            columns.forEach {
                strb.append """\tval ${it.ktField}: ${it.ktType}"""
                if (it.defaultValue != null) {
                    if (it.ktType.contains("String"))
                        strb.append " = \"${it.defaultValue}\""
                    else
                        strb.append " = ${it.defaultValue}"
                } else {
                    if (it.nullable) {
                        strb.append " = null"
                    }
                }
                strb.append ',\n'
            }
            strb.deleteCharAt(strb.lastIndexOf(','))
            strb.deleteCharAt(strb.lastIndexOf('\n'))
            return strb.toString()
        }

        def getCursorConstructor(ArrayList<Column> columns) {
            def strb = new StringBuilder("\tconstructor (cursor: Cursor) : this(\n")
            columns.forEach {
                strb.append "\t\t${it.ktField} = ${getCursorGetValue(it)},\n"
            }
            strb.deleteCharAt(strb.lastIndexOf(','))
            strb.deleteCharAt(strb.lastIndexOf('\n'))
            strb.append(')')
            return strb.toString()
        }

        def getCursorGetValue(Column col) {
            switch (col.ktType) {
                case 'String':
                case 'String?':
                case 'Short':
                case 'Short?':
                    return "cursor.getString(cursor.getColumnIndex(${col.nameUpper()}))"
                case 'Int':
                case 'Int?':
                    return "cursor.getInt(cursor.getColumnIndex(${col.nameUpper()}))"
                case 'Long':
                case 'Long?':
                    return "cursor.getLong(cursor.getColumnIndex(${col.nameUpper()}))"
                case 'Float':
                case 'Float?':
                    return "cursor.getFloat(cursor.getColumnIndex(${col.nameUpper()}))"
                case 'Double':
                case 'Double?':
                    return "cursor.getDouble(cursor.getColumnIndex(${col.nameUpper()}))"
                case 'Boolean':
                case 'Boolean?':
                    return "cursor.getDouble(cursor.getColumnIndex(${col.nameUpper()}))"

                default:
                    throw SqliteKtGenException("cursor getCustomValue not implemented")
            }
        }

        def getConstColumnName(List<Column> columns) {
            def strb = new StringBuilder()
            columns.forEach {
                strb.append """\t\tconst val ${it.name.toUpperCase()} = "${it.name}"\n"""
            }
            return strb.toString()
        }

        def getCreateTableQuery(Table table) {
            def strb = new StringBuilder('\t\tconst val CREATE_TABLE = """').append("CREATE TABLE ${table.name} (\n")
            table.columns.forEach {
                strb.append "\t\t\t${it.name} ${getDbType(it)}"

                if (!it.nullable)
                    strb.append " NOT NULL"
                strb.append " ${it.typeAppend ?: ""}"
                strb.append(",\n")
            }
            strb.deleteCharAt(strb.lastIndexOf(','))
            strb.append('\t\t)"""\n')
            return strb
        }

        def getDbType(Column col) {
            switch (col.ktType) {
                case 'String':
                case 'String?':
                case 'Short':
                case 'Short?':
                    return "TEXT"
                case 'Int':
                case 'Int?':
                case 'Long':
                case 'Long?':
                    return "INTEGER"
                case 'Float':
                case 'Float?':
                case 'Double':
                case 'Double?':
                    return "REAL"
                case 'Boolean':
                case 'Boolean?':
                    return "BOOLEAN"

                default:
                    return col.dbType
            }
        }

        def getConstQueries(Map<String, String> queries) {
            def strb = new StringBuilder()
            queries.forEach { key, values ->
                strb.append """\t\tconst val ${key.toUpperCase()} = "${values}"\n"""
            }
            return strb.toString()
        }
    }
}
