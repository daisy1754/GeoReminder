package jp.gr.java_conf.daisy.georeminder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import jp.gr.java_conf.daisy.georeminder.data.GeoSqliteOpenHelper
import jp.gr.java_conf.daisy.georeminder.data.Reminder
import jp.gr.java_conf.daisy.georeminder.data.ReminderQueryHelper
import kotlinx.android.synthetic.main.activity_new_reminder.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop

class NewReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder)
        val sqliteHelper = ReminderQueryHelper()
        val db = GeoSqliteOpenHelper(this).writableDatabase

        saveButton.setOnClickListener {
            // TODO: add validations for input
            
            sqliteHelper.insertReminder(db, Reminder(
                    latitudeInput.text.toString().toDouble(),
                    longitudeInput.text.toString().toDouble(),
                    radiusInput.text.toString().toInt(),
                    startTimeInput.text.toString(),
                    endTimeInput.text.toString(),
                    Reminder.NO_AUTO_DISMISS,
                    titleInput.text.toString(),
                    messageInput.text.toString()
            ))
            startActivity(intentFor<ItemListActivity>().singleTop().clearTop())
        }
    }
}
