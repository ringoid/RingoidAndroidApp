package com.ringoid.domain.repository.user

import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.CurrentUser
import io.reactivex.Single

interface IUserRepository {

    fun createUserProfile(essence: AuthCreateProfileEssence): Single<CurrentUser>
}
