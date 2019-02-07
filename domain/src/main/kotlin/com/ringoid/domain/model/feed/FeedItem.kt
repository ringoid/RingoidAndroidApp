package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.messenger.Message
import com.ringoid.utility.randomString

data class FeedItem(val isNotSeen: Boolean, val messages: List<Message>,
    override val id: String, override val images: List<Image>,
    override val isRealModel: Boolean = true) : IProfile {

    fun countOfPeerMessages(): Int = messages.count { it.peerId != DomainUtil.CURRENT_USER_ID }
    fun countOfUserMessages(): Int = messages.count { it.peerId == DomainUtil.CURRENT_USER_ID }
    fun hasPeerMessages(): Boolean = countOfPeerMessages() > 0

    fun profile(): Profile = Profile(id = id, images = images)
}

val EmptyFeedItem = FeedItem(isNotSeen = false, messages = emptyList(),
    id = randomString(), images = emptyList(), isRealModel = false)
