package com.ringoid.origin.feed.misc

object HandledPushDataInMemory {

    private var countOfHandledPushLikes: Int = 0
    private var countOfHandledPushMatches: Int = 0

    fun getCountOfHandledPushLikes(): Int = countOfHandledPushLikes
    fun getCountOfHandledPushMatches(): Int = countOfHandledPushMatches

    internal fun incrementCountOfHandledPushLikes() {
        ++countOfHandledPushLikes
    }

    internal fun incrementCountOfHandledPushMatches() {
        ++countOfHandledPushMatches
    }

    fun dropCountsOfHandledPush() {
        countOfHandledPushLikes = 0
        countOfHandledPushMatches = 0
    }
}
