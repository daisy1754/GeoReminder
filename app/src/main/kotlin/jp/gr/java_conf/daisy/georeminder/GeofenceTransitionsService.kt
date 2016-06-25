package jp.gr.java_conf.daisy.georeminder

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.Reminder
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper
import java.text.SimpleDateFormat
import java.util.*

class GeofenceTransitionsService : IntentService("GeofenceTransitoinsService") {

    override fun onHandleIntent(intent: Intent?) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event.hasError()) {
            Log.e(javaClass.simpleName, "Geofence error: ${event.errorCode}")
            return
        }

        val transition = event.geofenceTransition
        val requestId = event.triggeringGeofences.get(0).requestId.toLong()
        val sqliteHelper = ReminderQueryHelper()
        val db = GeoSqliteOpenHelper(this).readableDatabase
        val reminder = sqliteHelper.findReminderWithId(db, requestId)
        if (reminder == null) {
            Log.e(javaClass.simpleName, "Geofence is triggered with id '${requestId}' but corresponding data not found.")
            return
        }
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            if (!isCurrentTimeAccepted(reminder.startTime, reminder.endTime)) {
                Log.d(javaClass.simpleName, "Geofence is triggered with id '${requestId}' but "
                        + "notification is not shown as current time doesn't satisfy condition "
                        + "(start: ${reminder.startTime} - end: ${reminder.endTime})")
                return
            }

            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_place_white_36dp)
                    .setContentTitle(reminder.title)
                    .setContentText(reminder.message)
                    .build()
            NotificationManagerCompat.from(this).notify(requestId.toInt(), notification)
        } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            NotificationManagerCompat.from(this).cancelAll()
            Log.e(javaClass.simpleName, "Exit from zone. Remove notification")
        }
    }

    fun isCurrentTimeAccepted(startTime: String, endTime: String) : Boolean {
        val current = Calendar.getInstance()
        val start = Calendar.getInstance()
        start.time = SimpleDateFormat(Reminder.TIME_FORMAT).parse(startTime)
        val end = Calendar.getInstance()
        end.time = SimpleDateFormat(Reminder.TIME_FORMAT).parse(endTime)
        return (start.get(Calendar.HOUR_OF_DAY) <= current.get(Calendar.HOUR_OF_DAY))
                .and(start.get(Calendar.MINUTE) <= current.get(Calendar.MINUTE))
                .and(current.get(Calendar.HOUR_OF_DAY) <= end.get(Calendar.HOUR_OF_DAY))
                .and(current.get(Calendar.MINUTE) <= end.get(Calendar.MINUTE))
    }
}
