package com.ringoid.domain.model.feed

import com.ringoid.domain.model.IModel
import com.ringoid.domain.model.image.IImage

interface IProfile : IModel {

    val images: List<IImage>

    fun sameContent(other: IProfile): Boolean {
        if (images == other.images) {
            return true
        }

        if (images.size != other.images.size) {
            return false
        }

        // same size, diff order and content
        return images.toTypedArray() contentEquals other.images.toTypedArray()
    }
}
