package jp.gr.java_conf.daisy.georeminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import org.jetbrains.anko.toast

/**
 * Helper class for interacting with GeofencingApi. Note this class doesn't take care of connecting
 * or disconnecting with GoogleApiClient. Caller of this class needs to handle the connection.
 */
class GeofenceHelper(val context: Context, val googleApiClient: GoogleApiClient?) {

    fun createGeofence(id: Long, latitude: Double, longitude: Double, radiusMeters: Int): Geofence {
        Log.d(javaClass.simpleName, "geofence: ${latitude}, ${longitude}, ${radiusMeters}")
        return Geofence.Builder()
                .setRequestId(id.toString())
                .setCircularRegion(latitude, longitude, radiusMeters.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER.or(Geofence.GEOFENCE_TRANSITION_EXIT))
                .build()
    }

    fun setupGeofence(id: Long, latitude: Double, longitude: Double, radiusMeters: Int) {
        val geofence = createGeofence(id, latitude, longitude, radiusMeters)
        setupGeofence(listOf(geofence), {})
    }

    fun setupGeofence(geofences: List<Geofence>, onGeofenceUpdated: () -> Unit) {
        val request: GeofencingRequest = GeofencingRequest.Builder()
                .addGeofences(geofences)
                .build()

        val pendingIntent: PendingIntent = PendingIntent.getService(
                context,
                0,
                Intent(context, GeofenceTransitionsService::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT)

        LocationServices.GeofencingApi
                .addGeofences(googleApiClient, request, pendingIntent)
                .setResultCallback(object: ResultCallback<Status> {

                    override fun onResult(result: Status) {
                        if (result.isSuccess) {
                            onGeofenceUpdated()
                        } else {
                            Log.w(javaClass.simpleName, "geofencing register failure: ${result}")
                            context.toast(R.string.geofencing_register_error)
                        }
                    }
                })
    }
}
