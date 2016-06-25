package jp.gr.java_conf.daisy.georeminder

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper

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
        val correspondingGeofence = sqliteHelper.findReminderWithId(db, requestId)
        if (correspondingGeofence == null) {
            Log.e(javaClass.simpleName, "Geofence is triggered with id '${requestId}' but corresponding data not found.")
            return
        }
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // TODO: filter based on time
            val notification = NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_place_white_36dp)
                    .setContentTitle(correspondingGeofence.title)
                    .setContentText(correspondingGeofence.message)
                    .build()
            NotificationManagerCompat.from(this).notify(requestId.toInt(), notification)
        } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            NotificationManagerCompat.from(this).cancelAll()
            Log.e(javaClass.simpleName, "Exit from zone. Remove notification")
        }
    }
}
