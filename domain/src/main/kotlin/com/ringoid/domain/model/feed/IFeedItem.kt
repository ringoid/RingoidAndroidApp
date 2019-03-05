package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.Message

interface IFeedItem : IProfile {

    val messages: MutableList<Message>

    fun profile(): Profile = Profile(id = id, images = images)
    fun feedItem(): FeedItem = FeedItem(id = id, images = images, messages = messages, isNotSeen = false)

    fun countOfPeerMessages(): Int = messages.count { it.peerId != DomainUtil.CURRENT_USER_ID }
    fun countOfUserMessages(): Int = messages.count { it.peerId == DomainUtil.CURRENT_USER_ID }
}
