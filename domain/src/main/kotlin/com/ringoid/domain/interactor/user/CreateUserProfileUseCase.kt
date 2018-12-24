package com.ringoid.domain.interactor.user

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.CurrentUser
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Single

class CreateUserProfileUseCase(
    private val essence: AuthCreateProfileEssence, private val repository: IUserRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<CurrentUser>(threadExecutor, postExecutor) {

    override fun sourceImpl(): Single<CurrentUser> = repository.createUserProfile(essence)
}
