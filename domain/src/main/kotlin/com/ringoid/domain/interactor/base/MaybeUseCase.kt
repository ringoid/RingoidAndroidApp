package com.ringoid.domain.interactor.base

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import io.reactivex.Maybe
import io.reactivex.MaybeTransformer
import io.reactivex.schedulers.Schedulers

abstract class MaybeUseCase<T>(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(): Maybe<T>

    fun source(): Maybe<T> = sourceImpl().compose(transformer())

    private fun transformer(): MaybeTransformer<T, T> =
        MaybeTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
