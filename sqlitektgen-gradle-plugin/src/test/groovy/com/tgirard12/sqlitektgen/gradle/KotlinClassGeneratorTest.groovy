package com.tgirard12.sqlitektgen.gradle;

import spock.lang.Specification;

/**
 */
public class KotlinClassGeneratorTest extends Specification {

    def SqliteKtGenTask.KotlinClassGenerator classGenerator

    def setup() {
        classGenerator = new SqliteKtGenTask.KotlinClassGenerator()
    }

    Exception getException(Closure fun) {
        try {
            fun.run()
            return null
        } catch (Exception ex) {
            return ex
        }
    }

    def 'test classGenerator.getFields'() {
        when:
        def columns = [new Table.Column(ktField: "field1", ktType: "String"),
                       new Table.Column(ktField: "field2", ktType: "Long")]
        def fieldGen = classGenerator.getFields(columns)

        def fields = """\tvar field1: String
\tvar field2: Long
"""
        then:
        assert fieldGen == fields
    }

    def 'test classGenerator.getConstColumnName'() {
        when:
        def columns = [new Table.Column(name: "column_1"),
                       new Table.Column(name: "column_2")]
        def constColGen = classGenerator.getConstColumnName(columns)

        def columnsString = """\t\tconst val COLUMN_1 = "column_1"
\t\tconst val COLUMN_2 = "column_2"
"""
        then:
        assert constColGen == columnsString
    }

    def 'test classGenerator.getConstQueries'() {
        when:
        def queries = [query1: "select * from my_table",
                       query2: "select count(*) from my_table"] as HashMap<String, String>
        def queriesGen = classGenerator.getConstQueries(queries)

        def queriesString = """\t\tconst val QUERY1 = "select * from my_table"
\t\tconst val QUERY2 = "select count(*) from my_table"
"""
        then:
        assert queriesGen == queriesString
    }

    def 'test generate class with default values'() {
        when:
        def table = new Table(name: "my_table", ktClass: "my_table", ktPackage: "com.tgirard12.sqlitektgen",
                columns: [new Table.Column(name: "column_1", ktField: "column_1", ktType: "String", insertOrUpdate: true, select: true, typeAppend: ""),
                          new Table.Column(name: "column_2", ktField: "column_2", ktType: "String", insertOrUpdate: true, select: true, typeAppend: "")],
                queries: [query1: "select * from my_table",
                          query2: "select count(*) from my_table"] as HashMap<String, String>)


        def kotlinClassGen = classGenerator.getKotlinClass(table)

        def kotlinclass = """
package com.tgirard12.sqlitektgen

class my_table {
    var column_1: String
    var column_2: String

    companion object {
        const val TABLE_NAME = "my_table"
        const val COLUMN_1 = "column_1"
        const val COLUMN_2 = "column_2"

        const val QUERY1 = "select * from my_table"
        const val QUERY2 = "select count(*) from my_table"

    }
}
"""

        then:
        assert kotlinClassGen.expand(4) == kotlinclass
    }
}