package com.tgirard12.sqlitektgen.sample

import android.app.Application
import android.database.sqlite.SQLiteDatabase

class App : Application() {

    companion object {
        lateinit var appOpenHelper: AppSqliteOpenHelper
        val database: SQLiteDatabase by lazy { appOpenHelper.writableDatabase }
    }

    override fun onCreate() {
        super.onCreate()

        appOpenHelper = AppSqliteOpenHelper(this)
    }
}