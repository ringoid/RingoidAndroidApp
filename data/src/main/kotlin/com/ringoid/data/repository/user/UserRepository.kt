package com.ringoid.data.repository.user

import com.ringoid.data.local.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.CurrentUser
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(cloud: RingoidCloud, spm: SharedPrefsManager) : BaseRepository(cloud, spm), IUserRepository {

    // TODO: always check db first
    override fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser> =
            cloud.createUserProfile(essence).map { it.map() }
}
