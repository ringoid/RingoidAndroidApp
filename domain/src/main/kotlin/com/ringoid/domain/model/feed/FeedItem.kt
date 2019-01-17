package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.messenger.Message

data class FeedItem(val isNotSeen: Boolean, val messages: List<Message>,
    override val id: String, override val images: List<Image>) : IProfile
