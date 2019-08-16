package com.ringoid.domain.model.image

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.utility.randomInt

data class ImageRequest(
    val id: Int = randomInt(),
    val accessToken: String = "",
    val clientImageId: String = DomainUtil.BAD_ID,
    val originImageId: String = DomainUtil.BAD_ID,
    val extension: String = "",
    val imageFilePath: String = "",
    val type: String) {

    fun createRequestEssence(): ImageUploadUrlEssence =
        ImageUploadUrlEssence(accessToken = accessToken, clientImageId = clientImageId, extension = extension)

    fun deleteRequestEssence(): ImageDeleteEssence =
        ImageDeleteEssence(accessToken = accessToken, imageId = originImageId)

    companion object {
        const val TYPE_CREATE = "create"
        const val TYPE_DELETE = "delete"

        fun from(essence: ImageUploadUrlEssence, imageFilePath: String): ImageRequest =
            ImageRequest(accessToken = essence.accessToken, clientImageId = essence.clientImageId,
                         extension = essence.extension, imageFilePath = imageFilePath, type = TYPE_CREATE)

        fun from(essence: ImageDeleteEssence): ImageRequest =
            ImageRequest(accessToken = essence.accessToken, originImageId = essence.imageId, type = TYPE_DELETE)
    }
}
