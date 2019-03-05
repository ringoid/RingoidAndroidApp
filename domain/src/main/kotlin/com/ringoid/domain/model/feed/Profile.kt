package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.IImage
import com.ringoid.utility.randomString

data class Profile(override val id: String, override val images: List<IImage>,
                   override val isRealModel: Boolean = true) : IProfile

val EmptyProfile = Profile(id = randomString(), images = emptyList(), isRealModel = false)
