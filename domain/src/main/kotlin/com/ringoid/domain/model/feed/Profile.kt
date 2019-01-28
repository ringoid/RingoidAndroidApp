package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.Image
import com.ringoid.utility.randomString

data class Profile(override val id: String, override val images: List<Image>,
                   override val isRealModel: Boolean = true) : IProfile

val EmptyProfile = Profile(id = randomString(), images = emptyList(), isRealModel = false)
