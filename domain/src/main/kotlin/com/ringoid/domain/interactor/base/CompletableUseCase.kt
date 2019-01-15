package com.ringoid.domain.interactor.base

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class CompletableUseCase(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(params: Params = Params()): Completable

    fun source(params: Params = Params()): Completable =
        sourceImpl(params)
            .compose(transformer())
            .doOnSubscribe { Timber.v("Perform use case ${javaClass.simpleName} with params: $params") }

    private fun transformer(): CompletableTransformer =
        CompletableTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
