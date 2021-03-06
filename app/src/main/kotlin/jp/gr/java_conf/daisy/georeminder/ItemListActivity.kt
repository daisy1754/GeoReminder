package jp.gr.java_conf.daisy.georeminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper
import kotlinx.android.synthetic.main.activity_item_list.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

/**
 * Show list of all registered geofences.
 */
class ItemListActivity : AppCompatActivity() {

    val REQUEST_CODE_LOCATION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        fab.setOnClickListener { startActivity<NewReminderActivity>() }

        val queryHelper = ReminderQueryHelper()
        val db = GeoSqliteOpenHelper(this).readableDatabase

        val reminders = queryHelper.queryReminders(db)
        list.adapter = RemindersAdapter(reminders)
        if (BuildConfig.IS_DEVELOPER) {
            list.onItemClickListener = AdapterView.OnItemClickListener { listView, view, position, id ->
                GeofenceTransitionsService.startServiceForDebugging(this, reminders.get(position).id!!)
            }
        }
        updateVisibility(!reminders.isEmpty())
        list.onItemLongClickListener = AdapterView.OnItemLongClickListener { listView, view, position, id ->
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.delete_confirmation_message_format, reminders.get(position).title))
                    .setPositiveButton(android.R.string.ok, { dialog, i ->
                        queryHelper.deleteItemWithId(db, reminders.get(position).id!!)
                        val reminders = queryHelper.queryReminders(db)
                        list.adapter = RemindersAdapter(reminders)
                        updateVisibility(!reminders.isEmpty())
                    })
                    .show()
            return@OnItemLongClickListener true
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_geofence_map -> {
                startActivity(intentFor<MapActivity>())
                return true
            }
            R.id.action_show_license -> {
                startActivity(intentFor<ShowLicenseActivity>())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Snackbar.make(
                        findViewById(R.id.list),
                        R.string.permission_need_granted,
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    fun updateVisibility(hasReminder: Boolean) {
        noGeofenceMessage.visibility = if(hasReminder) View.GONE else View.VISIBLE
        list.visibility = if(hasReminder) View.VISIBLE else View.GONE
    }
}
