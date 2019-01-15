package com.ringoid.data.repository.user

import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.model.user.CurrentUser
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @Named("user") private val local: UserDao, cloud: RingoidCloud, spm: ISharedPrefsManager)
    : BaseRepository(cloud, spm), IUserRepository {

    override fun accessToken(): Single<AccessToken> = spm.accessSingle { Single.just(it) }

    // TODO: always check db first
    override fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser> =
            cloud.createUserProfile(essence)
                 .doOnSuccess {
                     spm.saveUserProfile(userId = it.userId, accessToken = it.accessToken)
                     local.addUserProfile(ProfileDbo(id = it.userId))
                 }
                 .map { it.map() }
}
