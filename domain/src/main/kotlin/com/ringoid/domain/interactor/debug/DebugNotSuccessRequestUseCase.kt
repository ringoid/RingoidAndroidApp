package com.ringoid.domain.interactor.debug

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.feed.IDebugRepository
import io.reactivex.Completable
import javax.inject.Inject

class DebugNotSuccessRequestUseCase @Inject constructor(val repository: IDebugRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable = repository.debugNotSuccessResponse()
}
