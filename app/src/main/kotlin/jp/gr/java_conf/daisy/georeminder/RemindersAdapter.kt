package jp.gr.java_conf.daisy.georeminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import jp.gr.java_conf.daisy.georeminder.data.Reminder
import org.jetbrains.anko.find

class RemindersAdapter(var reminders : List<Reminder>) : BaseAdapter() {

    override fun getCount(): Int {
        return reminders.size
    }

    override fun getView(index: Int, maybeView: View?, parentView: ViewGroup?): View? {
        var view = maybeView
        if (view == null) {
            view = LayoutInflater.from(parentView?.context).inflate(R.layout.list_item_reminder, parentView, false)
        }
        val item = getItem(index)
        view?.find<TextView>(R.id.title)?.text = item.title
        view?.find<TextView>(R.id.message)?.text = item.message
        view?.find<TextView>(R.id.timerange)?.text = "${item.startTime} - ${item.endTime}"
        view?.find<TextView>(R.id.location)?.text = "within ${item.radiusMeters}(m) from ${item.latitude},${item.longitude}"
        return view
    }

    override fun getItem(index: Int): Reminder {
        return reminders.get(index)
    }

    override fun getItemId(index: Int): Long {
        return reminders.get(index).hashCode().toLong()
    }
}
