package com.ringoid.data.repository.user

import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.repository.user.IUserRepository
import javax.inject.Singleton

@Singleton
class UserRepository(private val cloud: RingoidCloud) : IUserRepository {

    override fun createUserProfile(essence: AuthCreateProfileEssence) {
        // TODO
    }
}
