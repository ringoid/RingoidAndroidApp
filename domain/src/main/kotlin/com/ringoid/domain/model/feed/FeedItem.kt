package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.messenger.Message
import com.ringoid.utility.randomString

data class FeedItem(
    val isNotSeen: Boolean,
    override val id: String, override val images: List<IImage>,
    override val messages: MutableList<Message>,
    override val isRealModel: Boolean = true) : IFeedItem

val EmptyFeedItem = FeedItem(isNotSeen = false, messages = mutableListOf(),
    id = randomString(), images = emptyList(), isRealModel = false)
