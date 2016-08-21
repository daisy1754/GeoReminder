package jp.gr.java_conf.daisy.georeminder

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.Reminder
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper
import kotlinx.android.synthetic.main.activity_new_reminder.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Activity to register a new reminder.
 */
class NewReminderActivity : AppCompatActivity() {

    private val PLACE_PICKER_REQUEST = 1
    private var googleApiClient: GoogleApiClient? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

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
        val geofencingHelper = GeofenceHelper(this, googleApiClient)

        locationButton.setOnClickListener {
            startActivityForResult(
                    PlacePicker.IntentBuilder().build(this), PLACE_PICKER_REQUEST);
        }
        saveButton.setOnClickListener {
            if (latitude == null || longitude == null) {
                Snackbar.make(
                        rootView,
                        R.string.location_empty_error,
                        Snackbar.LENGTH_LONG)
                        .show();
                return@setOnClickListener
            }
            if (radiusInput.text.isEmpty()) {
                radiusInput.error = getString(R.string.generic_empty_error)
                return@setOnClickListener
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

            val radiusMeters = radiusInput.text.toString().toInt()
            val id = sqliteHelper.insertReminder(db, Reminder(
                    latitude!!,
                    longitude!!,
                    radiusMeters,
                    startTimeInput.text.toString(),
                    endTimeInput.text.toString(),
                    Reminder.NO_AUTO_DISMISS,
                    titleInput.text.toString(),
                    messageInput.text.toString()
            ))
            geofencingHelper.setupGeofence(id, latitude!!, longitude!!, radiusMeters)
            startActivity(intentFor<ItemListActivity>().singleTop().clearTop())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === PLACE_PICKER_REQUEST) {
            if (resultCode === RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                latitude = place.latLng.latitude
                longitude = place.latLng.longitude
                locationButtonText.text = getString(R.string.update_location_button)
                selectedLocationInfo.text = getString(R.string.selected_location, place.name,
                        place.latLng.latitude, place.latLng.longitude)
                selectedLocationInfo.visibility = View.VISIBLE
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
