package com.ringoid.domain.interactor.user

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processCompletable
import com.ringoid.domain.model.essence.user.ReferralCodeEssenceUnauthorized
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Completable
import javax.inject.Inject

class ApplyReferralCodeUseCase @Inject constructor(private val repository: IUserRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable =
        params.processCompletable(ReferralCodeEssenceUnauthorized::class.java, repository::applyReferralCode)
}
