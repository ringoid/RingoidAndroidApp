package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.Image

data class Profile(override val id: String, override val images: List<Image>) : IProfile
