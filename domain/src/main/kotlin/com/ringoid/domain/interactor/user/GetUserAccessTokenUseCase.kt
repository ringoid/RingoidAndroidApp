package com.ringoid.domain.interactor.user

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUserAccessTokenUseCase @Inject constructor(private val repository: IUserRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<AccessToken>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<AccessToken> = repository.accessToken()
}
