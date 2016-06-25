package jp.gr.java_conf.daisy.georeminder.data

data class Reminder(val latitude: Double,
                    val longitude: Double,
                    val radiusMeters: Int,
                    val startTime: String,
                    val endTime: String,
                    val autoDismissSecs: Int = NO_AUTO_DISMISS,
                    val title: String = "",
                    val message: String = "",
                    var id: Long? = null) {
    companion object {
        val NO_AUTO_DISMISS = -1
        val TIME_FORMAT = "hh:mm"
    }
}
