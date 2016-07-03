package jp.gr.java_conf.daisy.georeminder.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listen {@link Intent.ACTION_BOOT_COMPLETED} and re-configure geofences as Google Play service
 * will clear registered geofences when device is rebooted.
 */
class BootBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        context.startService(Intent(context, RestoreGeofencesService::class.java))
    }
}
