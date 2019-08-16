package com.ringoid.datainterface.local.user

import com.ringoid.domain.model.image.UserImage
import io.reactivex.Single

interface IUserImageDbFacade {

    fun addUserImage(image: UserImage)

    fun countUserImages(): Single<Int>

    fun deleteAllUserImages(): Int

    fun deleteUserImage(id: String): Int

    fun isUserImageBlockedByOriginId(originImageId: String): Boolean

    fun updateUserImage(image: UserImage): Int

    fun updateUserImageByOriginId(originImageId: String, uri: String,
                                  numberOfLikes: Int, isBlocked: Boolean,
                                  sortPosition: Int): Int

    fun userImage(id: String): Single<UserImage>

    fun userImages(): Single<List<UserImage>>
}
