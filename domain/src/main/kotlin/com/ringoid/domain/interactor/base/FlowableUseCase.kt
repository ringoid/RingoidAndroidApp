package com.ringoid.domain.interactor.base

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class FlowableUseCase<T>(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(params: Params = Params()): Flowable<T>

    fun source(params: Params = Params()): Flowable<T> =
        sourceImpl(params)
            .compose(transformer())
            .doOnSubscribe { Timber.v("Perform use case ${javaClass.simpleName} with params: $params") }

    private fun transformer(): FlowableTransformer<T, T> =
        FlowableTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
