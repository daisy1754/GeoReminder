package jp.gr.java_conf.daisy.georeminder

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.Reminder
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper
import org.jetbrains.anko.intentFor
import java.text.SimpleDateFormat
import java.util.*

/**
 * Triggered when user make a transition (enter or exit geofence) to show notification.
 */
class GeofenceTransitionsService : IntentService("GeofenceTransitoinsService") {

    companion object {
        const val INTENT_KEY_DEBUG = "debug"
        const val INTENT_KEY_GEOFENCE_ID = "geofenceId"

        fun startServiceForDebugging(context: Context, geofenceId: Long) {
            context.startService(context.intentFor<GeofenceTransitionsService>(
                    INTENT_KEY_DEBUG to true, INTENT_KEY_GEOFENCE_ID to geofenceId))
        }
    }

    override fun onHandleIntent(intent: Intent) {
        if (intent.getBooleanExtra(INTENT_KEY_DEBUG, false)) {
            handleGeofenceTransition(
                    Geofence.GEOFENCE_TRANSITION_ENTER,
                    intent.getLongExtra(INTENT_KEY_GEOFENCE_ID, -1L))
            return
        }

        val event = GeofencingEvent.fromIntent(intent)
        if (event.hasError()) {
            Log.e(javaClass.simpleName, "Geofence error: ${event.errorCode}")
            return
        }
        val requestId = event.triggeringGeofences.get(0).requestId.toLong()
        handleGeofenceTransition(event.geofenceTransition, requestId)
    }

    fun handleGeofenceTransition(transition: Int, requestId: Long) {
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
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(longArrayOf(300, 300))
                    .build()
            NotificationManagerCompat.from(this).notify(requestId.toInt(), notification)
            // In my environment (Android 6.0.1), notification doesn't vibrate if phone is in
            // silence mode. Explicitly calling vibrator here as I want phone to vibrate regardless.
            (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(300)
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
