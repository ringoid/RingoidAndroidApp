package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil

data class Lmm(val likes: List<FeedItem>, val matches: List<FeedItem>, val messages: List<FeedItem>) {

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

    fun newLikesCount(): Int = likes.count { it.isNotSeen }
    fun newMatchesCount(): Int = matches.count { it.isNotSeen }
    fun containsNotSeenItems(): Boolean = newLikesCount() > 0 || newMatchesCount() > 0
}
