package com.tgirard12.sqlitektgen.sample

import android.content.ContentValues
import android.database.Cursor

data class UserDb(
        val _id: Long = -1,
        val name: String,
        val email: String? = "",
        val createdAt: Long,
        val groupId: Long? = null,
        val groups: GroupDb? = null) {

    constructor (cursor: Cursor) : this(
            _id = cursor.getLong(cursor.getColumnIndex(_ID)),
            name = cursor.getString(cursor.getColumnIndex(NAME)),
            email = cursor.getString(cursor.getColumnIndex(EMAIL)),
            createdAt = cursor.getLong(cursor.getColumnIndex(CREATEDAT)),
            groupId = cursor.getLong(cursor.getColumnIndex(GROUPID)))

    companion object {
        const val TABLE_NAME = "UserDb"
        const val _ID = "_id"
        const val NAME = "name"
        const val EMAIL = "email"
        const val CREATEDAT = "createdAt"
        const val GROUPID = "groupId"

        const val CREATE_TABLE = """CREATE TABLE UserDb (
            _id INTEGER NOT NULL NOT NULL PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL ,
            email TEXT ,
            createdAt INTEGER NOT NULL ,
            groupId INTEGER 
        )"""

    }

    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
            cv.put(NAME, name)
            if (email == null) cv.putNull(EMAIL) else cv.put(EMAIL, email)
            cv.put(CREATEDAT, createdAt)
            if (groupId == null) cv.putNull(GROUPID) else cv.put(GROUPID, groupId)
            return cv
        }
}
