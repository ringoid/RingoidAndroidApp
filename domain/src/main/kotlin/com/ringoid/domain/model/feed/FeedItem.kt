package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.messenger.Message
import com.ringoid.utility.randomString

data class FeedItem(val isNotSeen: Boolean, val messages: List<Message>,
    override val id: String, override val images: List<Image>,
    override val isRealModel: Boolean = true) : IProfile {

    fun profile(): Profile = Profile(id = id, images = images)
}

val EmptyFeedItem = FeedItem(isNotSeen = false, messages = emptyList(),
    id = randomString(), images = emptyList(), isRealModel = false)
