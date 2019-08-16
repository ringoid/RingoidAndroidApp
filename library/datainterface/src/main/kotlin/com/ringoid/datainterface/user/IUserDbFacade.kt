package com.ringoid.datainterface.user

interface IUserDbFacade {

    fun addUserProfile(userId: String)

    fun deleteUserProfile(userId: String)
}
