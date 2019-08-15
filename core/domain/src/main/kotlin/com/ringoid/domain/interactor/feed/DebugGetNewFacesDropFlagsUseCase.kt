package com.ringoid.domain.interactor.feed

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.debug.IDebugFeedRepository
import io.reactivex.Completable
import javax.inject.Inject

class DebugGetNewFacesDropFlagsUseCase @Inject constructor(
    private val repository: IDebugFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable = repository.dropFlags()
}
