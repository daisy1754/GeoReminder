package jp.gr.java_conf.daisy.georeminder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_show_license.*

/**
 * Activity to show software licenses.
 */
class ShowLicenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_license)

        licenseText.adapter = LicenseContentAdapter(GoogleApiAvailability.getInstance()
                .getOpenSourceSoftwareLicenseInfo(this)!!)
    }

    class LicenseContentAdapter(var licenseText : String) : BaseAdapter() {

        val TEXT_PER_LOW = 10000

        override fun getCount(): Int {
            return (licenseText.length / TEXT_PER_LOW) + 1
        }

        override fun getView(index: Int, maybeView: View?, parentView: ViewGroup?): View? {
            var view: TextView
            if (maybeView == null) {
                view = TextView(parentView?.context)
            } else {
                view = maybeView as TextView
            }
            view.text = getItem(index)
            return view
        }

        override fun getItem(index: Int): String {
            return licenseText.substring(index * TEXT_PER_LOW, (index + 1) * TEXT_PER_LOW)
        }

        override fun getItemId(index: Int): Long {
            return index.toLong()
        }
    }
}
