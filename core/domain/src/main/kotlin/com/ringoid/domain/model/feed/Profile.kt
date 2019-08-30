package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.image.IImage
import com.ringoid.utility.randomString

data class Profile(
    override val id: String,
    override val distanceText: String? = null,
    override val images: List<IImage>,
    override val lastOnlineStatus: String? = null,
    override val lastOnlineText: String? = null,
    override val age: Int = DomainUtil.UNKNOWN_VALUE,
    override val children: Int = DomainUtil.UNKNOWN_VALUE,
    override val education: Int = DomainUtil.UNKNOWN_VALUE,
    override val gender: Gender = Gender.UNKNOWN,
    override val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    override val height: Int = DomainUtil.UNKNOWN_VALUE,
    override val income: Int = DomainUtil.UNKNOWN_VALUE,
    override val property: Int = DomainUtil.UNKNOWN_VALUE,
    override val transport: Int = DomainUtil.UNKNOWN_VALUE,
    override val about: String? = null,
    override val company: String? = null,
    override val jobTitle: String? = null,
    override val name: String? = null,
    override val instagram: String? = null,
    override val tiktok: String? = null,
    override val status: String? = null,
    override val university: String? = null,
    override val whereFrom: String? = null,
    override val whereLive: String? = null,
    override val isRealModel: Boolean = true) : IProfile

val EmptyProfile = Profile(id = randomString(), images = emptyList(), isRealModel = false)
