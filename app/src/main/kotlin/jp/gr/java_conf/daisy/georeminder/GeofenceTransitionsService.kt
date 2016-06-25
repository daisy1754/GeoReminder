package jp.gr.java_conf.daisy.georeminder

import android.app.IntentService
import android.content.Intent
import android.util.Log

class GeofenceTransitionsService : IntentService("GeofenceTransitoinsService") {

    override fun onHandleIntent(intent: Intent?) {
        Log.d(javaClass.simpleName, "geofence is triggered")
    }
}
