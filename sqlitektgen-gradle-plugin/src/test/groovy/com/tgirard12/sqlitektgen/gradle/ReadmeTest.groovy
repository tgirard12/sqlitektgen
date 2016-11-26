package com.tgirard12.sqlitektgen.gradle

import spock.lang.Specification


class ReadmeTest extends Specification {

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
[
  {
    "table": "User", "ktPackage": "com.tgirard12.sqlitektgen.sample",
    "columns":
    [
      {"name": "_id",    "ktType": "Long",    "defaultValue": -1,
          "insertOrUpdate": false,     "typeAppend": "NOT NULL PRIMARY KEY AUTOINCREMENT"},
      {"name": "name",   "ktType": "String" },
      {"name": "age",    "ktType": "Int?"   }
    ],
    "queries": {
      "COUNT_ALL": "select count(_id) from User",
      "SELECT_BY_NAME": "select * from User where name=?"
    },
    "selectBy": {
      "SELECT_BY_NAME_AND_AGE": "name,age"
    }
  }
]"""
        def table = parser.parseJsonContent(json)
        def generateClazz = classGenerator.getKotlinClass(table[0])

        def kotlinclass = """
package com.tgirard12.sqlitektgen.sample

import android.content.ContentValues
import android.database.Cursor

data class User(
        val _id: Long = -1,
        val name: String,
        val age: Int? = null) {

    constructor (cursor: Cursor) : this(
            _id = cursor.getLong(cursor.getColumnIndex(_ID)),
            name = cursor.getString(cursor.getColumnIndex(NAME)),
            age = if (cursor.isNull(cursor.getColumnIndex(AGE))) null else cursor.getInt(cursor.getColumnIndex(AGE)))

    companion object {
        const val TABLE_NAME = "User"
        const val _ID = "User._id"
        const val NAME = "User.name"
        const val AGE = "User.age"

        const val CREATE_TABLE = \"\"\"CREATE TABLE User (
            _id INTEGER NOT NULL NOT NULL PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL ,
            age INTEGER \n        )\"\"\"

        const val COUNT_ALL = "select count(_id) from User"
        const val SELECT_BY_NAME = "select * from User where name=?"
        const val SELECT_BY_NAME_AND_AGE = "SELECT * FROM User WHERE User.name=? AND User.age=?"
    }

    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
            cv.put(NAME, name)
            if (age == null) cv.putNull(AGE) else cv.put(AGE, age)
            return cv
        }
}
"""
        then:
        assert generateClazz.expand(4) == kotlinclass
    }
}