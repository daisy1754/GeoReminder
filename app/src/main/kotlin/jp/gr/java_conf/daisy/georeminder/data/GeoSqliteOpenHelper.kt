package jp.gr.java_conf.daisy.georeminder.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GeoSqliteOpenHelper : SQLiteOpenHelper {

    constructor(context: Context, dbName: String = "georeminder.db", version: Int = 1) : super(context, dbName, null, version) {
    }

    override fun onCreate(db: SQLiteDatabase?) {
        ReminderQueryHelper().createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}