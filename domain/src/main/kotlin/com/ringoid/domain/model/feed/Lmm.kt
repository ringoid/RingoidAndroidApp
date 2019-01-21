package com.ringoid.domain.model.feed

data class Lmm(val likes: List<FeedItem>, val matches: List<FeedItem>, val messages: List<FeedItem>) {

    fun isLikesEmpty(): Boolean = likes.isEmpty()
    fun isMatchesEmpty(): Boolean = matches.isEmpty()
    fun isMessagesEmpty(): Boolean = messages.isEmpty()

    fun newLikesCount(): Int = likes.count { it.isNotSeen }
    fun newMatchesCount(): Int = matches.count { it.isNotSeen }
    fun hasSomethingNew(): Boolean = newLikesCount() > 0 || newMatchesCount() > 0
}
