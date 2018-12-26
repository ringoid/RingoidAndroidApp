package com.ringoid.domain.interactor.user

import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.user.CurrentUser
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateUserProfileUseCase @Inject constructor(private val repository: IUserRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<CurrentUser>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<CurrentUser> =
        params.get(AuthCreateProfileEssence::class.java)?.let {
            repository.createUserProfile(it)
        } ?: Single.error<CurrentUser> { MissingRequiredParamsException() }
}
