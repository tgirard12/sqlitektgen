
[ ![Download](https://api.bintray.com/packages/tgirard12/android/sqlitektgen/images/download.svg) ](https://bintray.com/tgirard12/android/sqlitektgen/_latestVersion)

# sqlitektgen

Generate Kotlin data class for a better SQLite usage in Android

This project is inspired by SQLDelight but this one generate kotlin immutable data class (https://kotlinlang.org/docs/reference/data-classes.html)

It create a dedicate gradle task to generate kotlin files. This way there is no performance issue with database usage. 

# Exemple

Sqlitektgen use a json file to describe your database structure and generate kotlin class.

This is a minimalist database file for one table :

````json
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
    }
  }
]
````

The gradle `generateSqliteKtClasses` Task generate this kotlin data class : 

````kotlin
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
        const val _ID = "_id"
        const val NAME = "name"
        const val AGE = "age"

        const val CREATE_TABLE = """CREATE TABLE User (
            _id INTEGER NOT NULL NOT NULL PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL ,
            age INTEGER 
        )"""

        const val COUNT_ALL = "select count(_id) from User"
        const val SELECT_BY_NAME = "select * from User where name=?"
    }

    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
            cv.put(NAME, name)
            if (age == null) cv.putNull(AGE) else cv.put(AGE, age)
            return cv
        }
}
````

### build.gradle

````groovy
buildscript {
    dependencies { 
        classpath 'com.tgirard12:sqlitektgen:$sqlitektgen_version'
    }
}

apply plugin: 'com.tgirard12.sqlitektgen'


sqlitektgen {
    databaseFile 'mobile/src/sqldb/database.json'
    outputDir 'mobile/src/main/kotlin/com/tgirard12/sqlitektgen/sample/db/'
}

````

## Advanced usage

This is the available options for one database table :

````json
[
  {
    "name": "NAME OF THE TABLE",
    "ktClass": "KOTLIN CLASS AND FILE NAME",
    "ktPackage": "PACKAGE OF THE CLASS",
    
    "columns": [
      {
        "name": "NAME OF TH COLUMN IN THE DATABASE",
        "ktField": "KOTLIN FIELD",
        "ktType": "KOTLIN TYPE",
        "typeAppend": "OPTIONNAL DATABASE TYPE TO APPEND",
        "insertOrUpdate": "THE FIELD WILL NOT BE INSERED IN DATABASE",    
        "select": "THE FIELD WILL NOT BE UPDATED IN DATABASE",    
        "defaultValue": "OPTIONNAL KOTLIN FIELD DEFAULT VALUE",    
      }      
    ]
    "queries": {
      "name": "FULL SQL QUERIES (WITH ?)" 
    }
  }
]
````

# License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.