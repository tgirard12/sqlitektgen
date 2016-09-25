
package com.tgirard12.sqlitektgen.sample

import android.content.ContentValues
import android.database.Cursor

data class GroupDb (
    val groupId: Long = -1,
    val groupNname: String,
    val users: List<UserDb>? = null) {

    constructor (cursor: Cursor) : this(
        groupId = cursor.getLong(cursor.getColumnIndex(GROUPID)),
        groupNname = cursor.getString(cursor.getColumnIndex(GROUPNNAME)))

    companion object {
        const val TABLE_NAME = "GroupDb"
        const val GROUPID = "groupId"
        const val GROUPNNAME = "groupNname"

        const val CREATE_TABLE = """CREATE TABLE GroupDb (
            groupId INTEGER NOT NULL NOT NULL PRIMARY KEY AUTOINCREMENT,
            groupNname TEXT NOT NULL 
        )"""

    }

    val contentValue: ContentValues
        get() {
            val cv = ContentValues()
            cv.put(GROUPNNAME, groupNname)
            return cv
        }
}
