package com.ringoid.repository

import android.content.Context
import android.content.SharedPreferences
import com.ringoid.domain.DomainUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedSharedPrefs @Inject constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val SHARED_PREFS_FILE_NAME = "FeedRingoid.prefs"

        private const val SP_KEY_TOTAL_NOT_FILTERED_LIKES_COUNT = "sp_key_total_not_filtered_likes_count"
        private const val SP_KET_TOTAL_NOT_FILTERED_MESSAGES_COUNT = "sp_ket_total_not_filtered_messages_count"
    }

    internal fun getTotalNotFilteredLikes(): Int =
        sharedPreferences.getInt(SP_KEY_TOTAL_NOT_FILTERED_LIKES_COUNT, DomainUtil.BAD_VALUE)

    internal fun getTotalNotFilteredMessages(): Int =
        sharedPreferences.getInt(SP_KET_TOTAL_NOT_FILTERED_MESSAGES_COUNT, DomainUtil.BAD_VALUE)

    internal fun setTotalNotFilteredLikes(count: Int) {
        sharedPreferences.edit().putInt(SP_KEY_TOTAL_NOT_FILTERED_LIKES_COUNT, count).apply()
    }

    internal fun setTotalNotFilteredMessages(count: Int) {
        sharedPreferences.edit().putInt(SP_KET_TOTAL_NOT_FILTERED_MESSAGES_COUNT, count).apply()
    }

    internal fun dropTotalNotFilteredLikes() {
        sharedPreferences.edit().remove(SP_KEY_TOTAL_NOT_FILTERED_LIKES_COUNT).apply()
    }

    internal fun dropTotalNotFilteredMessages() {
        sharedPreferences.edit().remove(SP_KET_TOTAL_NOT_FILTERED_MESSAGES_COUNT).apply()
    }
}