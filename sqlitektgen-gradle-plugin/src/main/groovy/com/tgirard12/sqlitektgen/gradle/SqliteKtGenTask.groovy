package com.tgirard12.sqlitektgen.gradle

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class SqliteKtGenTask extends DefaultTask {

    SqliteKtGenExtension extension
    DatabaseFileParser databaseFileParser

    public SqliteKtGenTask() {

        databaseFileParser = new DatabaseFileParser()
    }

    @TaskAction
    def sqlitektenTask() {

        extension = project.extensions.getByType(SqliteKtGenExtension.class)
        checkExtensionParam()

        def tables = databaseFileParser.parseDatabaseFile(extension.databaseFile)

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
                    column.ktType = col.ktType ?: "String"
                    column.typeAppend = col.typeAppend ?: ""
                    if (col.insertOrUpdate == null)
                        column.insertOrUpdate = true
                    else
                        column.insertOrUpdate = col.insertOrUpdate
                    if (col.select == null)
                        column.select = true
                    else
                        column.select = col.select
                }
                table.queries = tab.queries
            }
            return tables
        }

        String isKtTypeAccepted(String ktType) {
            try {
                def type = Table.KtType.valueOf(ktType)
                return type.name()
            } catch (Exception ex) {
                throw new SqliteKtGenException("Type ${ktType} not supported. Supported types : ${Table.KtType.values().toString()}")
            }
        }
    }

    class KotlinClassGenerator {

        def getKotlinClass(Table table) {
            """
package ${table.ktPackage}

class ${table.ktClass} {
${getFields(table.columns)}
    companion object {
        const val TABLE_NAME = "${table.name}"
${getConstColumnName(table.columns)}
${getConstQueries(table.queries)}
${getFromCursor(table)}
    }
}
"""
        }

        def getFields(List<Table.Column> columns) {
            def strb = new StringBuilder()
            columns.forEach {
                strb.append """\tvar ${it.ktField}: ${it.ktType}\n"""
            }
            return strb.toString()
        }

        def getConstColumnName(List<Table.Column> columns) {
            def strb = new StringBuilder()
            columns.forEach {
                strb.append """\t\tconst val ${it.name.toUpperCase()} = "${it.name}"\n"""
            }
            return strb.toString()
        }

        def getConstQueries(Map<String, String> queries) {
            def strb = new StringBuilder()
            queries.forEach { key, values ->
                strb.append """\t\tconst val ${key.toUpperCase()} = "${values}"\n"""
            }
            return strb.toString()
        }

        def getFromCursor(Table table) {
            def strb = new StringBuilder("\t\tfun fromCursor(c: Cursor) {\n")
            strb.append("\t\t\tval _entry = ${table.ktClass}()\n")
            table.columns.forEach {
                if (it.select) {
                    strb.append "\t\t\t_entry.${it.ktField} = "
                    strb.append "c.get${it.ktType}(c.getColumnIndex(${it.name.toUpperCase()}))\n"
                }
            }
            strb.append("\t\t\treturn _entry\n")
            strb.append("\t\t}\n")
            return strb.toString()
        }
    }
}
