package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.Message

interface IFeedItem : IProfile {

    val messages: List<Message>

    fun profile(): Profile = Profile(id = id, age = age, images = images)
    fun feedItem(): FeedItem = FeedItem(id = id, age = age, images = images, messages = messages, isNotSeen = false)

    fun countOfPeerMessages(): Int = messages.count { it.peerId != DomainUtil.CURRENT_USER_ID }
    fun countOfUserMessages(): Int = messages.count { it.peerId == DomainUtil.CURRENT_USER_ID }
    fun countOfPeerMessages(messages: List<Message>): Int = messages.count { it.peerId != DomainUtil.CURRENT_USER_ID }
    fun countOfUserMessages(messages: List<Message>): Int = messages.count { it.peerId == DomainUtil.CURRENT_USER_ID }
}
