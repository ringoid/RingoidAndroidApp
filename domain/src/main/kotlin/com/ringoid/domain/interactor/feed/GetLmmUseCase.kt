package com.ringoid.domain.interactor.feed

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject

@Deprecated("LMM -> LC")
class GetLmmUseCase @Inject constructor(val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Lmm>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Lmm> {
        val source = params.get<String>("source")

        return params.processSingle(ImageResolution::class.java) {
            repository.getLmm(it, source = source)
        }
    }
}
