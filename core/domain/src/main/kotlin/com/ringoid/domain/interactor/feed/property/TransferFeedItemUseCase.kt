package com.ringoid.domain.interactor.feed.property

import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import javax.inject.Inject

class TransferFeedItemUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable {
        val profileId = params.get<String>("profileId")
        val destinationFeed = params.get<String>("destinationFeed")

        return if (profileId.isNullOrBlank() || destinationFeed.isNullOrBlank()) {
            Completable.error(MissingRequiredParamsException())
        } else {
            repository.transferFeedItem(feedItemId = profileId.orEmpty(), destinationFeed = destinationFeed.orEmpty())
        }
    }
}
