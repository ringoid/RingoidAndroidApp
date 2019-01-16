package com.ringoid.domain.repository.user

import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.model.user.CurrentUser
import io.reactivex.Completable
import io.reactivex.Single

interface IUserRepository {

    fun accessToken(): Single<AccessToken>

    fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser>

    fun deleteUserProfile(): Completable
}
