package com.ringoid.origin.feed.model

import androidx.annotation.DrawableRes
import com.ringoid.widget.R

enum class OnlineStatus(private val str: String, @DrawableRes val resId: Int) {
    ONLINE("online", R.drawable.online_status_oval),
    OFFLINE("offline", R.drawable.offline_status_oval),
    AWAY("away", R.drawable.away_status_oval),
    UNKNOWN("", 0);

    companion object {
        fun from(str: String?): OnlineStatus =
            when (str) {
                "online" -> ONLINE
                "offline" -> OFFLINE
                "away" -> AWAY
                else -> UNKNOWN
            }
    }
}
