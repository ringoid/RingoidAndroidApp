package com.ringoid.domain.model.image

import com.ringoid.utility.randomString
import java.io.File

data class LocalImage(override val id: String = randomString(), override val file: File? = null) : ILocalImage
