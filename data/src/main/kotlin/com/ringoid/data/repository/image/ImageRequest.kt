package com.ringoid.data.repository.image

import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.image.ILocalImage
import com.ringoid.utility.randomString

sealed class ImageRequest(val id: String = randomString())

class CreateImageRequest(id: String = randomString(), val image: IImage) : ImageRequest(id)

class CreateLocalImageRequest(id: String = randomString(), val image: ILocalImage) : ImageRequest(id)

class DeleteImageRequest(id: String = randomString(), val imageId: String) : ImageRequest(id)
