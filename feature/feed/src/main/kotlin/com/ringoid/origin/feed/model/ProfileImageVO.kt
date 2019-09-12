package com.ringoid.origin.feed.model

import com.ringoid.domain.model.IListModel
import com.ringoid.domain.model.image.EmptyImage
import com.ringoid.domain.model.image.IImage
import com.ringoid.utility.randomString

data class ProfileImageVO(val profileId: String, val image: IImage) : IListModel {

    override fun getModelId(): Long = ((217L + image.getModelId()) * 31L) + profileId.hashCode()

    fun idWithFirstN(N: Int = 3): String = profileId.substring(0..minOf(N, profileId.length - 1))
}

val EmptyProfileImageVO = ProfileImageVO(profileId = randomString(), image = EmptyImage)
