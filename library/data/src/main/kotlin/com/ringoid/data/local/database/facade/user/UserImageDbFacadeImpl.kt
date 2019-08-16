package com.ringoid.data.local.database.facade.user

import com.ringoid.data.local.database.dao.image.UserImageDao
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.datainterface.user.IUserImageDbFacade
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.model.mapList
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserImageDbFacadeImpl @Inject constructor(private val dao: UserImageDao) : IUserImageDbFacade {

    override fun addUserImage(image: UserImage) {
        UserImageDbo.from(image).also { dao.addUserImage(it) }
    }

    override fun countUserImages(): Single<Int> = dao.countUserImages()

    override fun deleteAllUserImages(): Int = dao.deleteAllUserImages()

    override fun deleteUserImage(id: String): Int = dao.deleteUserImage(id)

    override fun isUserImageBlockedByOriginId(originImageId: String): Boolean =
        dao.isUserImageBlockedByOriginId(originImageId)

    override fun updateUserImage(image: UserImage): Int =
        dao.updateUserImage(UserImageDbo.from(image))

    override fun updateUserImageByOriginId(
            originImageId: String,
            uri: String,
            numberOfLikes: Int,
            isBlocked: Boolean,
            sortPosition: Int): Int =
        dao.updateUserImageByOriginId(originImageId, uri, numberOfLikes, isBlocked, sortPosition)

    override fun userImage(id: String): Single<UserImage> = dao.userImage(id).map { it.map() }

    override fun userImages(): Single<List<UserImage>> = dao.userImages().map { it.mapList() }
}
