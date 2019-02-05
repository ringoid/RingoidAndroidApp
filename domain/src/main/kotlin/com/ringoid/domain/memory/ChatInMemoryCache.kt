package com.ringoid.domain.memory

object ChatInMemoryCache {

    val BOTTOM_CHAT_POSITION: Pair<Int, Int> = 0 to 0

    private val chatInputMessage = mutableMapOf<String, CharSequence>()
    private val chatMessagesCount = mutableMapOf<String, Int>()
    private val chatScrollPosition = mutableMapOf<String, Pair<Int, Int>>()

    // ------------------------------------------
    fun getInputMessage(profileId: String): CharSequence? = chatInputMessage[profileId]

    fun setInputMessage(profileId: String, text: CharSequence) {
        if (hasProfile(profileId)) {
            chatInputMessage[profileId] = text
        }
    }

    // ------------------------------------------
    fun getMessagesCount(profileId: String): Int = chatMessagesCount[profileId] ?: 0

    fun setMessagesCount(profileId: String, count: Int) {
        if (hasProfile(profileId)) {
            chatMessagesCount[profileId] = count
            dropPositionForProfile(profileId)
        }
    }

    // ------------------------------------------
    fun hasProfile(profileId: String): Boolean = chatScrollPosition.containsKey(profileId)

    fun getProfilePosition(profileId: String): Pair<Int, Int> =
            chatScrollPosition[profileId] ?: BOTTOM_CHAT_POSITION

    fun addProfileIfNotExists(profileId: String): Boolean {
        val result = hasProfile(profileId)
        if (!result) {
            chatScrollPosition[profileId] = BOTTOM_CHAT_POSITION
        }
        return result
    }

    fun addProfileWithPosition(profileId: String, position: Pair<Int, Int>): Boolean {
        val result = hasProfile(profileId)
        chatScrollPosition[profileId] = position
        return result
    }

    fun addProfileWithPositionIfNotExists(profileId: String, position: Pair<Int, Int>): Boolean {
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

    // ------------------------------------------
    fun clear() {
        chatInputMessage.clear()
        chatMessagesCount.clear()
        chatScrollPosition.clear()
    }
}
