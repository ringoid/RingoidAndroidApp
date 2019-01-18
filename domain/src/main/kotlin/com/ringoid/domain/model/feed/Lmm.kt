package com.ringoid.domain.model.feed

data class Lmm(val likes: List<FeedItem>, val matches: List<FeedItem>, val messages: List<FeedItem>) {

    fun isLikesEmpty(): Boolean = likes.isEmpty()
    fun isMatchesEmpty(): Boolean = matches.isEmpty()
    fun isMessagesEmpty(): Boolean = messages.isEmpty()
}
