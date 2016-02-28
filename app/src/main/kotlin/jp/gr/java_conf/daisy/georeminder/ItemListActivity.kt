package jp.gr.java_conf.daisy.georeminder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_item_list.*
import org.jetbrains.anko.startActivity

class ItemListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        fab.setOnClickListener { startActivity<NewReminderActivity>() }
    }
}
