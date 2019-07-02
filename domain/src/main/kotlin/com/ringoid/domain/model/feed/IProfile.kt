package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil
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

    // custom property accessors
    fun about(): String? = if (about != DomainUtil.BAD_PROPERTY) about else null
    fun company(): String? = if (company != DomainUtil.BAD_PROPERTY) company else null
    fun jobTitle(): String? = if (jobTitle != DomainUtil.BAD_PROPERTY) jobTitle else null
    fun name(): String? = if (name != DomainUtil.BAD_PROPERTY) name else null
    fun instagram(): String? = if (instagram != DomainUtil.BAD_PROPERTY) instagram else null
    fun tiktok(): String? = if (tiktok != DomainUtil.BAD_PROPERTY) tiktok else null
    fun university(): String? = if (university != DomainUtil.BAD_PROPERTY) university else null
    fun whereFrom(): String? = if (whereFrom != DomainUtil.BAD_PROPERTY) whereFrom else null
    fun whereLive(): String? = if (whereLive != DomainUtil.BAD_PROPERTY) whereLive else null
}
