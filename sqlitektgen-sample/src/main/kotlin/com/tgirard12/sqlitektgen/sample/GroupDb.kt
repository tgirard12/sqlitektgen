package com.tgirard12.sqlitektgen.sample

import android.content.ContentValues
import android.database.Cursor

data class GroupDb(
        val _id: Long = -1,
        val name: String,
        val users: List<UserDb>? = null) {

    constructor (cursor: Cursor) : this(
            _id = cursor.getLong(cursor.getColumnIndex(_ID)),
            name = cursor.getString(cursor.getColumnIndex(NAME)))

    companion object {
        const val TABLE_NAME = "GroupDb"
        const val _ID = "_id"
        const val NAME = "name"

        const val CREATE_TABLE = """CREATE TABLE GroupDb (
            _id INTEGER NOT NULL NOT NULL PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL 
        )"""

    }

    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
            cv.put(NAME, name)
            return cv
        }
}
