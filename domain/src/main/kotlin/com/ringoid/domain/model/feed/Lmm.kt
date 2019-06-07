package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil

data class Lmm(val likes: List<FeedItem>, val matches: List<FeedItem>, val messages: List<FeedItem>) {

    constructor(): this(emptyList(), emptyList(), emptyList())

    fun isLikesEmpty(): Boolean = likes.isEmpty()
    fun isMatchesEmpty(): Boolean = matches.isEmpty()
    fun isMessagesEmpty(): Boolean = messages.isEmpty()

    fun messagesCount(): Int =
        messages.takeIf { it.isNotEmpty() }
                ?.let { it.map { it.messages.size }.reduce { acc, i -> acc + i } }
                ?: 0

    fun peerMessagesCount(): Int =
        messages.takeIf { it.isNotEmpty() }
                ?.let {
                    it.map { it.messages.filter { it.peerId != DomainUtil.CURRENT_USER_ID }.size }
                      .reduce { acc, i -> acc + i }
                }
                ?: 0

    fun totalCount(): Int = likes.size + matches.size + messages.size

    fun notSeenLikesProfileIds() = likes.filter { it.isNotSeen }.map { it.id }
    fun notSeenMatchesProfileIds() = matches.filter { it.isNotSeen }.map { it.id }

    fun notSeenLikesCount(): Int = likes.count { it.isNotSeen }
    fun notSeenMatchesCount(): Int = matches.count { it.isNotSeen }
    fun containsNotSeenItems(): Boolean = notSeenLikesCount() > 0 || notSeenMatchesCount() > 0

    fun toLogString(): String = "likes[${likes.size}], matches[${matches.size}], messages=[${messages.size}]"
}
