package com.ringoid.data.repository.image

sealed class ImageRequest(val id: Int)

class CreateImageRequest(id: Int) : ImageRequest(id)

class DeleteImageRequest(id: Int, val imageId: String) : ImageRequest(id)
