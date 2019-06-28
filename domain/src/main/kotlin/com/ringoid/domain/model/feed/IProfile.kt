package com.ringoid.domain.model.feed

import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.IModel
import com.ringoid.domain.model.image.IImage

interface IProfile : IModel {

    val distanceText: String?
    val images: List<IImage>
    val lastOnlineStatus: String?
    val lastOnlineText: String?

    // properties
    val age: Int
    val children: Int
    val education: Int
    val gender: Gender
    val hairColor: Int
    val height: Int
    val income: Int
    val property: Int
    val transport: Int

    // custom properties
    val about: String?
    val company: String?
    val jobTitle: String?
    val name: String?
    val instagram: String?
    val tiktok: String?
    val university: String?
    val whereFrom: String?
    val whereLive: String?

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
