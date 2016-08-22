package jp.gr.java_conf.daisy.georeminder.boot

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import jp.gr.java_conf.daisy.georeminder.GeofenceHelper
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper

/**
 * Re-register geofences to GoogleApiClient based on persisted data in the local database.
 */
class RestoreGeofencesService: IntentService("RestoreGeofencesService") {

    override fun onHandleIntent(intent: Intent) {
        val queryHelper = ReminderQueryHelper()
        val db = GeoSqliteOpenHelper(this).readableDatabase
        val reminders = queryHelper.queryReminders(db)

        Log.d(javaClass.simpleName, "RestoreGeofencesService start")
        if (reminders.isEmpty()) {
            return
        }

        val googleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()
        val result = googleApiClient.blockingConnect()
        if (result.isSuccess) {
            val helper = GeofenceHelper(this, googleApiClient)
            val geofences = reminders.map {
                helper.createGeofence(it.id!!, it.latitude, it.longitude, it.radiusMeters)
            }
            helper.setupGeofence(geofences, {
                Log.d(javaClass.simpleName, "Geofence is restored :)")
                googleApiClient.disconnect()
            })
        } else {
            Log.e(javaClass.simpleName, "Failed to connect Google API client ")
        }
    }
}
