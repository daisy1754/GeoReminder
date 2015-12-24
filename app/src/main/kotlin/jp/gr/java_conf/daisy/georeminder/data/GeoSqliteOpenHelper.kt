package jp.gr.java_conf.daisy.georeminder.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GeoSqliteOpenHelper : SQLiteOpenHelper {

    val reminderTable = "reminder"
    val latitude = "latitude"
    val longitude = "longitude"
    val radiusMeters = "radius_meters"
    val startTime = "start_time"
    val endTime = "end_time"
    val autoDismissSecs = "auto_dismiss_secs"
    val title = "title"
    val message = "message"

    constructor(context: Context, dbName: String = "georeminder.db", version: Int = 1) : super(context, dbName, null, version) {
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE ${reminderTable}(
                ${latitude} REAL,
                ${longitude} REAL,
                ${radiusMeters} INTEGER,
                ${startTime} TEXT,
                ${endTime} TEXT,
                ${autoDismissSecs} INTEGER,
                ${title} TEXT,
                ${message} TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}