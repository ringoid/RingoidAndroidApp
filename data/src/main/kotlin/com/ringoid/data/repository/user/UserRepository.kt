package com.ringoid.data.repository.user

import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.CurrentUser
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Single
import javax.inject.Singleton

@Singleton
class UserRepository(private val cloud: RingoidCloud) : IUserRepository {

    // TODO: always check db first
    override fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser> =
            cloud.createUserProfile(essence).map { it.map() }
}
