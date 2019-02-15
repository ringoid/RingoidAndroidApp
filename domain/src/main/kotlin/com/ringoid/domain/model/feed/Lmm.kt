package com.ringoid.domain.model.feed

data class Lmm(val likes: List<FeedItem>, val matches: List<FeedItem>, val messages: List<FeedItem>) {

    fun isLikesEmpty(): Boolean = likes.isEmpty()
    fun isMatchesEmpty(): Boolean = matches.isEmpty()
    fun isMessagesEmpty(): Boolean = messages.isEmpty()

    fun messagesCount(): Int = if (messages.isEmpty()) 0 else messages.map { it.messages.size }.reduce { acc, i -> acc + i }
    fun newLikesCount(): Int = likes.count { it.isNotSeen }
    fun newMatchesCount(): Int = matches.count { it.isNotSeen }
    fun containsNotSeenItems(): Boolean = newLikesCount() > 0 || newMatchesCount() > 0
}
