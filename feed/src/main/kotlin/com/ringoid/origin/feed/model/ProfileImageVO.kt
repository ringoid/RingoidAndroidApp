package com.ringoid.origin.feed.model

import com.ringoid.domain.model.image.EmptyImage
import com.ringoid.domain.model.image.IImage
import com.ringoid.utility.randomString

data class ProfileImageVO(val profileId: String, val image: IImage, var isLiked: Boolean = false)

val EmptyProfileImageVO = ProfileImageVO(profileId = randomString(), image = EmptyImage, isLiked = false)
