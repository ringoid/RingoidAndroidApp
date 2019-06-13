package com.ringoid.domain.interactor.feed.property

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.model.feed.property.LikedFeedItemIds
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject

@Deprecated("Since Transition")
class GetLikedFeedItemIdsUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<LikedFeedItemIds>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<LikedFeedItemIds> =
        params.processSingle("feedItemIds", repository::getLikedFeedItemIds)
}
