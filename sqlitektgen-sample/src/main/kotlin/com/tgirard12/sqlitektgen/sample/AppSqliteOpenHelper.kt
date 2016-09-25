package com.tgirard12.sqlitektgen.sample

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class AppSqliteOpenHelper(
        context: Context?)
: SQLiteOpenHelper(
        context,
        "sqlitektgen.database",
        null,
        1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(UserDb.CREATE_TABLE)
        db.execSQL(GroupDb.CREATE_TABLE)

        db.insert(GroupDb.TABLE_NAME, null, GroupDb(groupId = 1L, groupNname = "Github", users = null).contentValue)
        db.insert(GroupDb.TABLE_NAME, null, GroupDb(groupId = 2L, groupNname = "Google", users = null).contentValue)
        db.insert(GroupDb.TABLE_NAME, null, GroupDb(groupId = 2L, groupNname = "Twitter", users = null).contentValue)

        db.insert(UserDb.TABLE_NAME, null, UserDb(name = "John", email = "john@mail.com", createdAt = Date().time, groupId = 1L, group = null).contentValue)
        db.insert(UserDb.TABLE_NAME, null, UserDb(name = "James", email = "james@mail.com", createdAt = Date().time, groupId = 1L, group = null).contentValue)
        db.insert(UserDb.TABLE_NAME, null, UserDb(name = "Gary", email = "gary@gmail.com", createdAt = Date().time, groupId = 2L, group = null).contentValue)
        db.insert(UserDb.TABLE_NAME, null, UserDb(name = "Jake", email = "jake@gmail.com", createdAt = Date().time, groupId = 3L, group = null).contentValue)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}

/*
    Helpers methods
 */
fun <R> Cursor.first(body: (cursor: Cursor) -> R): R? {
    this.use {
        if (!this.moveToFirst())
            return null

        return body(this)
    }
}

fun <R> Cursor.list(body: (cursor: Cursor) -> R): List<R> {

    val mutableList = mutableListOf<R>()
    this.use {
        if (!this.moveToFirst())
            return listOf()
        do {
            mutableList.add(body(this))

        } while (this.moveToNext())

        return mutableList.toList()
    }
}