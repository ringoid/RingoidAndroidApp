package com.ringoid.domain.repository.user

import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.essence.user.ReferralCodeEssenceUnauthorized
import com.ringoid.domain.model.essence.user.UpdateUserProfileEssenceUnauthorized
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.model.user.CurrentUser
import io.reactivex.Completable
import io.reactivex.Single

interface IUserRepository {

    fun accessToken(): Single<AccessToken>
    fun applyReferralCode(essence: ReferralCodeEssenceUnauthorized): Completable

    fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser>

    fun deleteUserProfile(): Completable
    fun deleteUserLocalData(): Completable

    fun doOnLogout(): Completable

    fun updateUserProfile(essence: UpdateUserProfileEssenceUnauthorized): Completable
    fun updateUserSettings(essence: UpdateUserSettingsEssenceUnauthorized): Completable
}
