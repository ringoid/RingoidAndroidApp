package com.ringoid.domain.model.feed

import com.ringoid.domain.model.image.Image
import com.ringoid.utility.randomString

data class Profile(override val id: String, override val images: List<Image>) : IProfile

val EmptyProfile = Profile(id = randomString(), images = emptyList())
