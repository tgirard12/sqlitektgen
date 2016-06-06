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
        [ { "name": "string_null" },
          { "name": "string_not_null", "ktType": "String", "defaultValue": "" },
          { "name": "long_null", "ktType": "Long?" },
          { "name": "float_default_value", "ktType": "Float", "defaultValue": "0" } ]
} ]"""
        def table = parser.parseJsonContent(json)
        def generateClazz = classGenerator.getKotlinClass(table[0])

        def kotlinclass = """
package com.tgirard12.sqlitektgen

import android.content.ContentValues
import android.database.Cursor

data class my_table (
    val string_null: String? = null,
    val string_not_null: String = "",
    val long_null: Long? = null,
    val float_default_value: Float = 0) {

    constructor (cursor: Cursor) : this(
        string_null = cursor.getString(cursor.getColumnIndex(STRING_NULL)),
        string_not_null = cursor.getString(cursor.getColumnIndex(STRING_NOT_NULL)),
        long_null = cursor.getLong(cursor.getColumnIndex(LONG_NULL)),
        float_default_value = cursor.getFloat(cursor.getColumnIndex(FLOAT_DEFAULT_VALUE)))

    companion object {
        const val TABLE_NAME = "my_table"
        const val STRING_NULL = "string_null"
        const val STRING_NOT_NULL = "string_not_null"
        const val LONG_NULL = "long_null"
        const val FLOAT_DEFAULT_VALUE = "float_default_value"

        const val CREATE_TABLE = \"\"\"CREATE TABLE my_table (
            string_null TEXT ,
            string_not_null TEXT ,
            long_null INTEGER ,
            float_default_value REAL \n        )\"\"\"

    }

    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
            if (string_null == null) cv.putNull(STRING_NULL) else cv.put(STRING_NULL, string_null)
            cv.put(STRING_NOT_NULL, string_not_null)
            if (long_null == null) cv.putNull(LONG_NULL) else cv.put(LONG_NULL, long_null)
            cv.put(FLOAT_DEFAULT_VALUE, float_default_value)
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