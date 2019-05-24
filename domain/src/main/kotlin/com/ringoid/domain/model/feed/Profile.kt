package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.IImage
import com.ringoid.utility.randomString

data class Profile(
    override val id: String, override val distanceText: String? = null,
    override val images: List<IImage>,
    override val lastOnlineStatus: String? = null,
    override val lastOnlineText: String? = null,
    override val isRealModel: Boolean = true) : IProfile

val EmptyProfile = Profile(id = randomString(), distanceText = null, images = emptyList(),
                           lastOnlineStatus = null, lastOnlineText = null, isRealModel = false)
