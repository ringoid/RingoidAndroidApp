package com.ringoid.domain.interactor.base

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import io.reactivex.Maybe
import io.reactivex.MaybeTransformer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class MaybeUseCase<T>(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(params: Params = Params()): Maybe<T>

    fun source(params: Params = Params()): Maybe<T> =
        sourceImpl()
            .compose(transformer())
            .doOnSubscribe { Timber.v("Perform use case ${javaClass.simpleName} with params: $params") }

    private fun transformer(): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
