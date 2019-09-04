package com.ringoid.domain.interactor.feed

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject

class GetCachedLmmFeedItemIdsUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<List<String>>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<List<String>> = repository.getLmmProfileIds()
}
