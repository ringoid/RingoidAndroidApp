package com.ringoid.domain.memory

object ChatInMemoryCache {

    const val BOTTOM_CHAT_POSITION: Int = 0

    private val chatScrollPosition = mutableMapOf<String, Int>()

    fun hasProfile(profileId: String): Boolean = chatScrollPosition.containsKey(profileId)

    fun getProfilePosition(profileId: String): Int =
            chatScrollPosition[profileId] ?: BOTTOM_CHAT_POSITION

    fun addProfileIfNotExists(profileId: String): Boolean {
        val result = hasProfile(profileId)
        if (!result) {
            chatScrollPosition[profileId] = BOTTOM_CHAT_POSITION
        }
        return result
    }

    fun addProfileWithPosition(profileId: String, position: Int): Boolean {
        val result = hasProfile(profileId)
        chatScrollPosition[profileId] = position
        return result
    }

    fun addProfileWithPositionIfNotExists(profileId: String, position: Int): Boolean {
        val result = hasProfile(profileId)
        if (!result) {
            chatScrollPosition[profileId] = position
        }
        return result
    }

    fun dropPositionForProfile(profileId: String) {
        if (hasProfile(profileId)) {
            chatScrollPosition[profileId] = BOTTOM_CHAT_POSITION
        }
    }

    fun clear() {
        chatScrollPosition.clear()
    }
}
