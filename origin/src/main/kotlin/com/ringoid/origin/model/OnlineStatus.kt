package com.ringoid.origin.model

import androidx.annotation.DrawableRes
import com.ringoid.widget.R

enum class OnlineStatus(private val str: String, @DrawableRes val resId: Int) {
    ONLINE("online", R.drawable.online_status_oval),
    OFFLINE("offline", R.drawable.offline_status_oval),
    AWAY("away", R.drawable.away_status_oval),
    UNKNOWN("", 0);

    var label: String = ""
        private set

    companion object {
        fun from(str: String?, label: String? = ""): OnlineStatus =
            when (str) {
                "online" -> ONLINE
                "offline" -> OFFLINE
                "away" -> AWAY
                else -> UNKNOWN
            }
            .apply { this.label = label ?: "" }
    }
}
