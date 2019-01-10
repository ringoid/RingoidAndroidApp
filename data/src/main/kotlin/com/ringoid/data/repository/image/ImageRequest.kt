package com.ringoid.data.repository.image

import com.ringoid.domain.model.image.IImage

sealed class ImageRequest(val id: Int)

class CreateImageRequest(id: Int, val image: IImage) : ImageRequest(id)

class DeleteImageRequest(id: Int, val imageId: String) : ImageRequest(id)
