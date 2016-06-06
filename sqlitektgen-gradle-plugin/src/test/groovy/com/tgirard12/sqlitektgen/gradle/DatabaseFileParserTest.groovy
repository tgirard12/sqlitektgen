package com.tgirard12.sqlitektgen.gradle

import spock.lang.Specification
import spock.lang.Unroll;

/**
 */
public class DatabaseFileParserTest extends Specification {

    def SqliteKtGenTask.DatabaseFileParser parser

    def setup() {
        parser = new SqliteKtGenTask.DatabaseFileParser()
    }

    Exception getException(Closure fun) {
        try {
            fun.run()
            return null
        } catch (Exception ex) {
            return ex
        }
    }

    def 'test database file not exist throw exception'() {

        when:
        parser.parseDatabaseFile('notExistingFile.json')

        then:
        thrown(SqliteKtGenException.class)
    }

    def "test 'table' field required"() {
        when:
        def json = """
[ { } ]
"""
        def exception = getException { parser.parseJsonContent(json) }

        then:
        assert exception instanceof SqliteKtGenException
        assert exception.message == "'table' field required"
    }

    def "test 'package' field required"() {
        when:
        def json = """
[ { "table": "my_table" } ]
"""
        def exception = getException { parser.parseJsonContent(json) }

        then:
        assert exception instanceof SqliteKtGenException
        assert exception.message == "'ktPackage' field required"
    }

    def "test 'columns' must be > 0"() {
        when:
        def json = """
[ {
    "table": "my_table",    "ktPackage": "com.tgirard12.sqlitektgen",
    "columns": [ ]
    } ]
"""
        def exception = getException { parser.parseJsonContent(json) }

        then:
        assert exception instanceof SqliteKtGenException
        assert exception.message == "'columns' object must have at least one field"
    }

    def "test 'columns.name' required"() {
        when:
        def json = """
[ {
    "table": "my_table",    "ktPackage": "com.tgirard12.sqlitektgen",
    "columns":
        [ {  } ]
} ]
"""
        def exception = getException { parser.parseJsonContent(json) }

        then:
        assert exception instanceof SqliteKtGenException
        assert exception.message == "'columns.name' field required"
    }

    def 'default value required if nullable type'() {
        when:
        def json = """
[ {
    "table": "my_table",    "ktPackage": "com.tgirard12.sqlitektgen",
    "columns":
        [ { "name": "column_1", "ktType": "String" } ]
} ]
"""
        then:
        def exception = getException { parser.parseJsonContent(json) }

        then:
        assert exception instanceof SqliteKtGenException
        assert exception.message == "Column 'column_1': Nullable type required default value"

    }

    def 'test parse Table with default fields'() {
        when:
        def json = """
[ {
    "table": "my_table",    "ktPackage": "com.tgirard12.sqlitektgen",
    "columns":
        [ { "name": "column_1" },
          { "name": "column_2" } ],
    "queries": {
        "query1": "select * from my_table",
        "query2": "select count(*) from my_table"
    }
} ]"""
        def table = parser.parseJsonContent(json)

        def expectTable = [new Table(name: "my_table", ktClass: "my_table", ktPackage: "com.tgirard12.sqlitektgen",
                columns: [new Table.Column(name: "column_1", ktField: "column_1", ktType: "String?", insertOrUpdate: true, select: true, typeAppend: "", defaultValue: "null", nullable: true),
                          new Table.Column(name: "column_2", ktField: "column_2", ktType: "String?", insertOrUpdate: true, select: true, typeAppend: "", defaultValue: "null", nullable: true)],
                queries: [query1: "select * from my_table",
                          query2: "select count(*) from my_table"] as HashMap<String, String>)]

        then:
        assert table == expectTable
    }

    def 'test parse Table with all fields'() {

        when:
        def json = """
[ {
    "table": "my_table",    "ktClass": "MyTable" ,     "ktPackage": "com.tgirard12.sqlitektgen",
    "columns":
        [ { "name": "column_1",  "ktField": "column1", "ktType": "Long", "defaultValue": "-1", "typeAppend": "PRIMARY KEY",  "insertOrUpdate": true,  "select": false  },
          { "name": "column_2",  "ktField": "other2",  "ktType": "Int?",  "typeAppend": "NOT NULL",    "insertOrUpdate": false,  "select": true   } ],
    "queries": {
        "query1": "select * from my_table",
        "query2": "select count(*) from my_table"
    }
} ]"""
        def table = parser.parseJsonContent(json)

        def expectTable = [new Table(name: "my_table", ktClass: "MyTable", ktPackage: "com.tgirard12.sqlitektgen",
                columns: [new Table.Column(name: "column_1", ktField: "column1", ktType: "Long", defaultValue: "-1", insertOrUpdate: true, select: false, typeAppend: "PRIMARY KEY", nullable: false),
                          new Table.Column(name: "column_2", ktField: "other2", ktType: "Int?", defaultValue: "null", insertOrUpdate: false, select: true, typeAppend: "NOT NULL", nullable: true)],
                queries: [query1: "select * from my_table",
                          query2: "select count(*) from my_table"] as HashMap<String, String>)]

        then:
        assert table == expectTable
    }
}