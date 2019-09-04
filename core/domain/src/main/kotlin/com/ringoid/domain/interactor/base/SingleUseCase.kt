package com.ringoid.domain.interactor.base

import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.schedulers.Schedulers

abstract class SingleUseCase<T>(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(params: Params = Params()): Single<T>

    fun source(params: Params = Params()): Single<T> =
        sourceImpl(params)
            .compose(transformer())
            .doOnSubscribe { DebugLogUtil.v("Perform use case ${javaClass.simpleName} with params: $params") }

    private fun transformer(): SingleTransformer<T, T> =
        SingleTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
