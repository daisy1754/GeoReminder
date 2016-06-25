package jp.gr.java_conf.daisy.georeminder

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.Reminder
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper
import kotlinx.android.synthetic.main.activity_new_reminder.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import org.jetbrains.anko.toast
import java.text.ParseException
import java.text.SimpleDateFormat

class NewReminderActivity : AppCompatActivity() {

    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder)
        val sqliteHelper = ReminderQueryHelper()
        val db = GeoSqliteOpenHelper(this).writableDatabase
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, object: GoogleApiClient.OnConnectionFailedListener {

                    override fun onConnectionFailed(result: ConnectionResult) {
                        Log.e(javaClass.simpleName, "Failed to connect to GoogleApiClient ${result}")
                    }
                })
                .addApi(LocationServices.API)
                .build()

        saveButton.setOnClickListener {
            for (view in arrayOf(latitudeInput, longitudeInput, radiusInput)) {
                if (view.text.isEmpty()) {
                    view.error = getString(R.string.generic_empty_error)
                    return@setOnClickListener
                }
            }

            for (timeInput in arrayOf(startTimeInput, endTimeInput)) {
                try {
                    // Making sure format of time is valid
                    SimpleDateFormat(Reminder.TIME_FORMAT).parse(timeInput.text.toString())
                } catch (e: ParseException) {
                    timeInput.error = getString(R.string.invalid_time_error)
                    return@setOnClickListener
                }
            }
            // TODO: add more validations for input

            val latitude = latitudeInput.text.toString().toDouble()
            val longitude = longitudeInput.text.toString().toDouble()
            val radiusMeters = radiusInput.text.toString().toInt()
            val id = sqliteHelper.insertReminder(db, Reminder(
                    latitude,
                    longitude,
                    radiusMeters,
                    startTimeInput.text.toString(),
                    endTimeInput.text.toString(),
                    Reminder.NO_AUTO_DISMISS,
                    titleInput.text.toString(),
                    messageInput.text.toString()
            ))
            setupGeofence(id, latitude, longitude, radiusMeters)
            startActivity(intentFor<ItemListActivity>().singleTop().clearTop())
        }
    }

    fun setupGeofence(id: Long, latitude: Double, longitude: Double, radiusMeters: Int) {
        val geofence: Geofence = Geofence.Builder()
                .setRequestId(id.toString())
                .setCircularRegion(latitude, longitude, radiusMeters.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER.or(Geofence.GEOFENCE_TRANSITION_EXIT))
                .build()

        val request: GeofencingRequest = GeofencingRequest.Builder()
                .addGeofences(listOf(geofence))
                .build()

        val pendingIntent: PendingIntent = PendingIntent.getService(
                this,
                0,
                Intent(this, GeofenceTransitionsService::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT)

        Log.d(javaClass.simpleName, "geofence ${geofence} | ${latitude}, ${longitude}, ${radiusMeters}")
        LocationServices.GeofencingApi
                .addGeofences(googleApiClient, request, pendingIntent)
                .setResultCallback(object: ResultCallback<Status> {

                    override fun onResult(result: Status) {
                        if (!result.isSuccess) {
                            Log.w(javaClass.simpleName, "geofencing register failure: ${result}")
                            toast(R.string.geofencing_register_error)
                        }
                    }
                })
    }
}
