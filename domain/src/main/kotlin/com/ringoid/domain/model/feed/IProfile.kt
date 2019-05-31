package com.ringoid.domain.model.feed

import com.ringoid.domain.model.IModel
import com.ringoid.domain.model.image.IImage

interface IProfile : IModel {

    val distanceText: String?
    val images: List<IImage>
    val lastOnlineStatus: String?
    val lastOnlineText: String?

    // properties
    val age: Int
    val education: Int
    val hairColor: Int
    val height: Int
    val income: Int
    val property: Int
    val transport: Int

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
