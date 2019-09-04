package com.ringoid.domain.interactor.base

import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers

abstract class ObservableUseCase<T>(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(params: Params = Params()): Observable<T>

    fun source(params: Params = Params()): Observable<T> =
        sourceImpl(params)
            .compose(transformer())
            .doOnSubscribe { DebugLogUtil.v("Perform use case ${javaClass.simpleName} with params: $params") }

    private fun transformer(): ObservableTransformer<T, T> =
        ObservableTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
