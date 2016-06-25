package jp.gr.java_conf.daisy.georeminder

import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper

class MapActivity : AppCompatActivity() {

    val DEFAULT_ZOOM_LEVEL = 15.toFloat()
    var googleApiClient : GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val db = GeoSqliteOpenHelper(this).readableDatabase
        val reminders = ReminderQueryHelper().queryReminders(db)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)!! as SupportMapFragment;
        mapFragment.getMapAsync { map ->
            for (reminder in reminders) {
                val latlong = LatLng(reminder.latitude, reminder.longitude)
                map.addCircle(CircleOptions()
                        .center(latlong)
                        .radius(reminder.radiusMeters.toDouble()))
                map.addMarker(MarkerOptions()
                        .position(latlong)
                        .title("${reminder.title} (${reminder.startTime} - ${reminder.endTime})"))
            }
            updateMapAccordingToCurrentLocation(map)
        };
    }

    private fun updateMapAccordingToCurrentLocation(map: GoogleMap) {
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, object: GoogleApiClient.OnConnectionFailedListener {

                    override fun onConnectionFailed(result: ConnectionResult) {
                        Log.e(javaClass.simpleName, "Failed to connect to GoogleApiClient ${result}")
                    }
                })
                .addConnectionCallbacks(object: GoogleApiClient.ConnectionCallbacks{
                    override fun onConnectionSuspended(cause: Int) {}

                    override fun onConnected(bundle: Bundle?) {
                        val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                        if (!updateMapLocation(map, location)) {
                            val request = LocationRequest.create()
                                    .setNumUpdates(1)
                                    .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                                    .setInterval(0)
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    googleApiClient, request, {location -> updateMapLocation(map, location)})

                        }
                    }
                })
                .addApi(LocationServices.API)
                .build()
    }

    private fun updateMapLocation(map: GoogleMap, location: Location?) : Boolean {
        if (location != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM_LEVEL))
            return true
        }
        return false
    }
}
