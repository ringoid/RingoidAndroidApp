package com.ringoid.domain.interactor.feed

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.debug.IDebugRepository
import com.ringoid.utility.DebugOnly
import io.reactivex.Single
import javax.inject.Inject

@DebugOnly
class DebugGetNewFacesFailedAndRecoverAfterNTimesUseCase @Inject constructor(
    private val repository: IDebugRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Feed>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Feed> =
        params.processSingle("count", repository::debugGetNewFacesFailAndRecoverAfterNTimes)
}
