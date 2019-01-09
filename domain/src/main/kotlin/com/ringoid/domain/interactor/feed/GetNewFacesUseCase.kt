package com.ringoid.domain.interactor.feed

import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject

class GetNewFacesUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Feed>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Feed> {
        val limit = params.get<Int>("limit")
        val resolution = params.get<String>("resolution")

        if (limit == null || resolution.isNullOrBlank()) {
            throw MissingRequiredParamsException()
        }

        return repository.getNewFaces(resolution, limit)
    }
}
