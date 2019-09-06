package com.ringoid.repository.user

import com.ringoid.data.handleError
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.di.PerUser
import com.ringoid.datainterface.local.user.IUserDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.essence.user.*
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.model.user.CurrentUser
import com.ringoid.domain.repository.user.IUserRepository
import com.ringoid.report.log.Report
import com.ringoid.repository.BaseRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @PerUser private val local: IUserDbFacade, cloud: IRingoidCloudFacade,
    spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IUserRepository {

    override fun accessToken(): Single<AccessToken> = spm.accessSingle { Single.just(it) }

    override fun applyReferralCode(essence: ReferralCodeEssenceUnauthorized): Completable =
        spm.accessSingle { cloud.applyReferralCode(ReferralCodeEssence.from(essence, it.accessToken)) }
            .handleError(tag = "applyReferralCode", traceTag = "auth/claim")
            .ignoreElement()  // convert to Completable

    override fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser> =
        cloud.createUserProfile(essence)
             .handleError(tag = "createUserProfile", traceTag = "auth/create_profile")
             .doOnSuccess {
                 spm.saveUserProfile(userId = it.userId, userGender = Gender.from(essence.sex),
                                     userYearOfBirth = essence.yearOfBirth, accessToken = it.accessToken)
                 local.addUserProfile(userId = it.userId)
                 Report.setUser(spm.currentUserId())
             }
             .map { it.map() }

    override fun deleteUserProfile(): Completable =
        spm.accessSingle { cloud.deleteUserProfile(accessToken = it.accessToken) }
            .handleError(tag = "deleteUserProfile", traceTag = "auth/delete")
            .doOnSuccess { clearUserLocalData() }
            .ignoreElement()  // convert to Completable

    override fun deleteUserLocalData(): Completable = Completable.fromCallable { clearUserLocalData() }

    override fun updateUserProfile(essence: UpdateUserProfileEssenceUnauthorized): Completable =
        spm.accessSingle { cloud.updateUserProfile(UpdateUserProfileEssence.from(essence, it.accessToken)) }
            .handleError(tag = "updateUserProfile", traceTag = "auth/update_profile")
            .ignoreElement()  // convert to Completable

    override fun updateUserSettings(essence: UpdateUserSettingsEssenceUnauthorized): Completable =
        spm.accessSingle { cloud.updateUserSettings(UpdateUserSettingsEssence.from(essence, it.accessToken)) }
            .handleError(tag = "updateUserSettings", traceTag = "auth/update_settings")
            .ignoreElement()  // convert to Completable

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
