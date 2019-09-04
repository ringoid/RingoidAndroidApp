package com.ringoid.domain.interactor.feed

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject

class GetCachedFeedItemByIdUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<FeedItem>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<FeedItem> =
        params.processSingle("profileId", repository::getCachedFeedItemById)
}
