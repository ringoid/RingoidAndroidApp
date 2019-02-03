package com.ringoid.domain.interactor.feed

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.debug.IDebugFeedRepository
import io.reactivex.Single
import javax.inject.Inject

class DebugGetNewFacesRetryNTimesForPageUseCase @Inject constructor(
    private val repository: IDebugFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Feed>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Feed> {
        val page = params.get<Int>("page") ?: 0
        val failPage = params.get<Int>("failPage") ?: 0
        val count = params.get<Int>("count") ?: 2
        return repository.debugGetNewFacesWithFailNTimesBeforeSuccessForPage(page = page, failPage = failPage, count = count)
    }
}
