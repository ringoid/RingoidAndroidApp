package com.ringoid.domain.interactor.feed

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject

class GetNewFacesUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Feed>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Feed> {
        val limit = params.get<Int>("limit")

        return params.processSingle(ImageResolution::class.java) {
            repository.getNewFaces(resolution = it, limit = limit)
        }
    }
}
