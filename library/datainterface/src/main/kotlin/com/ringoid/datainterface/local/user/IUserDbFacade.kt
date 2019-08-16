package com.ringoid.datainterface.local.user

interface IUserDbFacade {

    fun addUserProfile(userId: String)

    fun deleteUserProfile(userId: String)
}
