package com.ringoid.origin.feed.misc

object HandledPushDataInMemory {

    private var countOfHandledPushLikes: Int = 0
    private var countOfHandledPushMatches: Int = 0
    private var countOfHandledPushMessages: Int = 0

    fun getCountOfHandledPushLikes(): Int = countOfHandledPushLikes
    fun getCountOfHandledPushMatches(): Int = countOfHandledPushMatches
    fun getCountOfHandledPushMessages(): Int = countOfHandledPushMessages

    internal fun incrementCountOfHandledPushLikes() {
        ++countOfHandledPushLikes
    }

    internal fun incrementCountOfHandledPushMatches() {
        ++countOfHandledPushMatches
    }

    internal fun incrementCountOfHandledPushMessages() {
        ++countOfHandledPushMessages
    }

    fun dropCountsOfHandledPush() {
        countOfHandledPushLikes = 0
        countOfHandledPushMatches = 0
        countOfHandledPushMessages = 0
    }
}
