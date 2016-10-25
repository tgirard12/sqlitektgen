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
          { "name": "date_no_db",           "ktType": "java.util.Date?",
                "insertOrUpdate": false,        "select": false,   },
          { "name": "string_not_null",      "ktType": "String"                          },
          { "name": "string_defaultValue",  "ktType": "String", "defaultValue": "OK"    },
          { "name": "boolean_null",         "ktType": "Boolean?"                         },
          { "name": "long_null",            "ktType": "Long?"                           },
          { "name": "float_default_value",  "ktType": "Float",  "defaultValue": "0"     },
          { "name": "int_no_insert_pk",     "ktType": "Int",    "defaultValue": "-1",
                "insertOrUpdate": false,  "typeAppend": "PRIMARY KEY AUTOINCREMENT"     } ]
} ]"""
        def table = parser.parseJsonContent(json)
        def generateClazz = classGenerator.getKotlinClass(table[0])

        def kotlinclass = """
package com.tgirard12.sqlitektgen

import android.content.ContentValues
import android.database.Cursor

data class my_table(
        val string_null: String? = null,
        val date_no_db: java.util.Date? = null,
        val string_not_null: String,
        val string_defaultValue: String = "OK",
        val boolean_null: Boolean? = null,
        val long_null: Long? = null,
        val float_default_value: Float = 0,
        val int_no_insert_pk: Int = -1) {

    constructor (cursor: Cursor) : this(
            string_null = if (cursor.isNull(cursor.getColumnIndex(STRING_NULL))) null else cursor.getString(cursor.getColumnIndex(STRING_NULL)),
            string_not_null = cursor.getString(cursor.getColumnIndex(STRING_NOT_NULL)),
            string_defaultValue = cursor.getString(cursor.getColumnIndex(STRING_DEFAULTVALUE)),
            boolean_null = if (cursor.isNull(cursor.getColumnIndex(BOOLEAN_NULL))) null else cursor.getInt(cursor.getColumnIndex(BOOLEAN_NULL)) > 0,
            long_null = if (cursor.isNull(cursor.getColumnIndex(LONG_NULL))) null else cursor.getLong(cursor.getColumnIndex(LONG_NULL)),
            float_default_value = cursor.getFloat(cursor.getColumnIndex(FLOAT_DEFAULT_VALUE)),
            int_no_insert_pk = cursor.getInt(cursor.getColumnIndex(INT_NO_INSERT_PK)))

    companion object {
        const val TABLE_NAME = "my_table"
        const val STRING_NULL = "string_null"
        const val STRING_NOT_NULL = "string_not_null"
        const val STRING_DEFAULTVALUE = "string_defaultValue"
        const val BOOLEAN_NULL = "boolean_null"
        const val LONG_NULL = "long_null"
        const val FLOAT_DEFAULT_VALUE = "float_default_value"
        const val INT_NO_INSERT_PK = "int_no_insert_pk"

        const val CREATE_TABLE = \"\"\"CREATE TABLE my_table (
            string_null TEXT ,
            string_not_null TEXT NOT NULL ,
            string_defaultValue TEXT NOT NULL ,
            boolean_null BOOLEAN ,
            long_null INTEGER ,
            float_default_value REAL NOT NULL ,
            int_no_insert_pk INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT\n        )\"\"\"

    }

    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
            if (string_null == null) cv.putNull(STRING_NULL) else cv.put(STRING_NULL, string_null)
            cv.put(STRING_NOT_NULL, string_not_null)
            cv.put(STRING_DEFAULTVALUE, string_defaultValue)
            if (boolean_null == null) cv.putNull(BOOLEAN_NULL) else cv.put(BOOLEAN_NULL, boolean_null)
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