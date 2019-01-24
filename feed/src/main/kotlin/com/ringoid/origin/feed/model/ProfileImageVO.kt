package com.ringoid.origin.feed.model

import com.ringoid.domain.model.IListModel
import com.ringoid.domain.model.image.EmptyImage
import com.ringoid.domain.model.image.IImage
import com.ringoid.utility.randomString

data class ProfileImageVO(val profileId: String, val image: IImage, var isLiked: Boolean = false) : IListModel {

    override fun getModelId(): Long = ((217L + image.getModelId()) * 31L) + profileId.hashCode()
}

val EmptyProfileImageVO = ProfileImageVO(profileId = randomString(), image = EmptyImage, isLiked = false)
