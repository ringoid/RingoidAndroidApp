package com.ringoid.domain.interactor.push

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processCompletable
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.repository.push.IPushRepository
import io.reactivex.Completable
import javax.inject.Inject

class UpdatePushTokenUseCase @Inject constructor(private val repository: IPushRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable =
        params.processCompletable(PushTokenEssenceUnauthorized::class.java, repository::updatePushToken)
}
