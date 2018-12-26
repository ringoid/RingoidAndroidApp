package com.ringoid.data.remote.model.image

import com.ringoid.domain.model.image.Image

/**
 * {
 *   "photoId":"12dd",
 *   "photoUri":"https://bla-bla.com/sss.jpg"
 * }
 */
class ImageEntity(id: String, uri: String) : BaseImageEntity<Image>(id = id, uri = uri) {

    override fun map(): Image = Image(id = id, uri = uri)
}
