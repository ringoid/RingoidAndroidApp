package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.Message
import com.ringoid.utility.randomString

data class FeedItem(val isNotSeen: Boolean,
    override val id: String, override val age: Int,
    override val distanceText: String? = null,
    override val images: List<IImage>,
    override val messages: MutableList<Message>,
    override val lastOnlineStatus: String? = null,
    override val lastOnlineText: String? = null,
    override val isRealModel: Boolean = true) : IFeedItem {

    fun toShortString(): String = "FeedItem(id=${id.substring(0..3)}, ${if (isNotSeen) "Not Seen" else "Seen"}, img=[${images.size}], msg=[${messages.size}])"
}

val EmptyFeedItem = FeedItem(isNotSeen = false, messages = mutableListOf(),
    id = randomString(), age = 0, images = emptyList(), isRealModel = false)
