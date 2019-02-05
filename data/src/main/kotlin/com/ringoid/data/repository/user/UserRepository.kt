package com.ringoid.data.repository.user

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.SentryUtil
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.model.user.CurrentUser
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @Named("user") private val local: UserDao, cloud: RingoidCloud,
    spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IUserRepository {

    override fun accessToken(): Single<AccessToken> = spm.accessSingle { Single.just(it) }

    // TODO: always check db first
    override fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser> =
        cloud.createUserProfile(essence)
             .handleError()  // TODO: notify on error
             .doOnSuccess {
                 spm.saveUserProfile(userId = it.userId, accessToken = it.accessToken)
                 local.addUserProfile(ProfileDbo(id = it.userId))
                 SentryUtil.setUser(spm)
             }
             .map { it.map() }

    override fun deleteUserProfile(): Completable =
        spm.accessSingle { cloud.deleteUserProfile(accessToken = it.accessToken) }
            .handleError()  // TODO: notify on error
            .doOnSuccess { clearUserLocalData() }
            .ignoreElement()  // convert to Completable

    override fun deleteUserLocalData(): Completable =
        Completable.fromCallable { clearUserLocalData() }

    // ------------------------------------------
    private fun clearUserLocalData() {
        spm.apply {
            currentUserId()?.let {
                local.deleteUserProfile(userId = it)
                deleteUserProfile(userId = it)
            }
        }
    }
}
