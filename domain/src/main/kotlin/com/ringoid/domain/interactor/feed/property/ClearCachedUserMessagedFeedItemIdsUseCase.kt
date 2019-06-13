package com.ringoid.domain.interactor.feed.property

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import javax.inject.Inject

@Deprecated("Since Transition")
class ClearCachedUserMessagedFeedItemIdsUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable = repository.clearCachedUserMessagedFeedItemIds()
}
