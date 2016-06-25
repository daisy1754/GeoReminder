package jp.gr.java_conf.daisy.georeminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper
import kotlinx.android.synthetic.main.activity_item_list.*
import org.jetbrains.anko.startActivity

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
        list.onItemLongClickListener = AdapterView.OnItemLongClickListener { listView, view, position, id ->
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.delete_confirmation_message_format, reminders.get(position).title))
                    .setPositiveButton(android.R.string.ok, { dialog, i ->
                        queryHelper.deleteItemWithId(db, reminders.get(position).id!!)
                        list.adapter = RemindersAdapter(queryHelper.queryReminders(db))
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
}
