package com.ringoid.origin.feed.model

import com.ringoid.domain.model.image.IImage

data class ProfileImageVO(val image: IImage, var isLiked: Boolean = false)
