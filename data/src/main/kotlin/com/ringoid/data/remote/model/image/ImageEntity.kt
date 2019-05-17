package com.ringoid.data.remote.model.image

import com.ringoid.domain.model.image.Image

/**
 * {
 *   "photoId":"12dd",
 *   "photoUri":"https://bla-bla.com/sss.jpg",
 *   "thumbnailPhotoUri":"https://bla.jpg"
 * }
 */
class ImageEntity(id: String, uri: String, thumbnailUri: String? = null)
    : BaseImageEntity<Image>(id = id, uri = uri, thumbnailUri = thumbnailUri) {

    override fun map(): Image = Image(id = id, uri = uri, thumbnailUri = thumbnailUri)
}
