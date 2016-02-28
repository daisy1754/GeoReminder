package jp.gr.java_conf.daisy.georeminder.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class ReminderQueryHelper {

    val reminderTable = "reminder"
    val latitude = "latitude"
    val longitude = "longitude"
    val radiusMeters = "radius_meters"
    val startTime = "start_time"
    val endTime = "end_time"
    val autoDismissSecs = "auto_dismiss_secs"
    val title = "title"
    val message = "message"

    fun createTable(db: SQLiteDatabase?) {
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

    fun insertReminder(db: SQLiteDatabase, reminder: Reminder) {
        val values = ContentValues()
        values.put(latitude, reminder.latitude)
        values.put(longitude, reminder.longitude)
        values.put(radiusMeters, reminder.radiusMeters)
        values.put(startTime, reminder.startTime)
        values.put(endTime, reminder.endTime)
        values.put(autoDismissSecs, reminder.autoDismissSecs)
        values.put(title, reminder.title)
        values.put(message, reminder.message)
        db.insert(reminderTable, null, values)
    }
}