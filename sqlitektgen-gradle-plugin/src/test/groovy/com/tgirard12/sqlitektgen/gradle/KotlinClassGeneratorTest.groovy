package com.tgirard12.sqlitektgen.gradle;

import spock.lang.Specification;

/**
 */
public class KotlinClassGeneratorTest extends Specification {

    SqliteKtGenTask.KotlinClassGenerator classGenerator
    SqliteKtGenTask.DatabaseFileParser parser

    def setup() {
        classGenerator = new SqliteKtGenTask.KotlinClassGenerator()
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

    def 'test json default value to kotlin class'() {
        when:
        def json = """
[ {
    "table": "my_table", "ktPackage": "com.tgirard12.sqlitektgen",
    "columns":
        [ { "name": "column_1" },
          { "name": "column_2", "ktType": "Long?" },
          { "name": "column_3", "ktType": "Float", "defaultValue": "0" } ]
} ]"""
        def table = parser.parseJsonContent(json)
        def generateClazz = classGenerator.getKotlinClass(table[0])

        def kotlinclass = """
package com.tgirard12.sqlitektgen

data class my_table (
    val column_1: String? = null,
    val column_2: Long? = null,
    val column_3: Float = 0) {

    constructor (cursor: Cursor) {
        column_1 = cursor.getString(cursor.getColumnIndex(COLUMN_1))
        column_2 = cursor.getLong(cursor.getColumnIndex(COLUMN_2))
        column_3 = cursor.getFloat(cursor.getColumnIndex(COLUMN_3)))
    }

    companion object {
        const val TABLE_NAME = "my_table"
        const val COLUMN_1 = "column_1"
        const val COLUMN_2 = "column_2"
        const val COLUMN_3 = "column_3"

        const val CREATE_TABLE = \"\"\"CREATE TABLE my_table (
            column_1 TEXT ,
            column_2 INTEGER ,
            column_3 REAL \n        )\"\"\"

    }

    val contentValue: ContentValue
        get() {
            val cv: ContentValue
            if (column_1 == null) cv.putNull(COLUMN_1) else cv.put(COLUMN_1, column_1)
            if (column_2 == null) cv.putNull(COLUMN_2) else cv.put(COLUMN_2, column_2)
            cv.put(COLUMN_3, column_3)
            return cv
        }
}
"""
        then:
        assert generateClazz.expand(4) == kotlinclass
    }
}
//    String name
//    String ktField
//    String ktType
//    String typeAppend
//    Boolean insertOrUpdate
//    Boolean select
//    String defaultValue

//    def 'test full json to kotlin class'() {
//        when:
//        def json = """
//[ {
//    "table": "my_table", "ktClass": "MyTable", "ktPackage": "com.tgirard12.sqlitektgen",
//    "columns":
//        [ { "name": "stringNull" },
//          { "name": "string_not_null", "ktField": "stringNotNull", "ktType": "String", "defaultValue": "sqlitektgen" },
//          { "name": "intNull", "ktType": "Int?" },
//          { "name": "long_not_null", "ktField": "longNotNull", "ktType": "Long", "defaultValue": "-1" }
//           ]
//} ]"""
//        def table = parser.parseJsonContent(json)
//        def generateClazz = classGenerator.getKotlinClass(table[0])
//
//        def kotlinclass = """
//package com.tgirard12.sqlitektgen
//
//data class MyTable (
//    val stringNull: String? = null,
//    val stringNotNull: String = "sqlitektgen",
//    val intNull: Int? = null,
//    val longNotNull: Long = -1
//) {
//
//    companion object {
//        const val TABLE_NAME = "my_table"
//        const val STRINGNULL = "stringNull"
//        const val STRING_NOT_NULL = "string_not_null"
//        const val INTNULL = "intNull"
//        const val LONG_NOT_NULL = "long_not_null"
//
//        const val CREATE_TABLE = \"\"\"CREATE TABLE my_table (
//            stringNull TEXT ,
//            string_not_null TEXT ,
//            intNull INTEGER ,
//            long_not_null INTEGER \n        )\"\"\"
//
//
//        fun fromCursor(cursor: Cursor) {
//            return MyTable(
//                stringNull = cursor.getString(cursor.getColumnIndex(STRINGNULL)),
//                stringNotNull = cursor.getString(cursor.getColumnIndex(STRING_NOT_NULL)),
//                intNull = cursor.getInt(cursor.getColumnIndex(INTNULL)),
//                longNotNull = cursor.getLong(cursor.getColumnIndex(LONG_NOT_NULL)))
//        }
//
//    }
//}
//"""
//        then:
//        assert generateClazz.expand(4) == kotlinclass
//    }
//}