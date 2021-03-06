package com.ringoid.data.local.database.facade.user

import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.model.feed.UserProfileDbo
import com.ringoid.datainterface.local.user.IUserDbFacade
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDbFacadeImpl @Inject constructor(private val dao: UserDao) : IUserDbFacade {

    override fun addUserProfile(userId: String) {
        dao.addUserProfile(UserProfileDbo(id = userId))
    }

    override fun deleteUserProfile(userId: String) = dao.deleteUserProfile(userId)
}
