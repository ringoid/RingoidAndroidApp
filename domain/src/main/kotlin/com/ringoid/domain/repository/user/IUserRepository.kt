package com.ringoid.domain.repository.user

import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence

interface IUserRepository {

    // TODO: return value
    fun createUserProfile(essence: AuthCreateProfileEssence)
}
