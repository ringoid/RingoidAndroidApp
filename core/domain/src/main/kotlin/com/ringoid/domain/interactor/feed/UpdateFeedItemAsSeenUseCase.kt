package com.ringoid.domain.interactor.feed

import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import javax.inject.Inject

class UpdateFeedItemAsSeenUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable {
        val feedItemId = params.get<String>("feedItemId")
        val isNotSeen = params.get<Boolean>("isNotSeen") ?: true

        return feedItemId.takeIf { !it.isNullOrBlank() }
            ?.let { repository.markFeedItemAsSeen(feedItemId = it, isNotSeen = isNotSeen) }
            ?: Completable.error(MissingRequiredParamsException())
    }
}
