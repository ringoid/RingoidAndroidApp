package com.orcchg.githubuser.domain.interactor.base

import com.orcchg.githubuser.domain.executor.UseCasePostExecutor
import com.orcchg.githubuser.domain.executor.UseCaseThreadExecutor
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.schedulers.Schedulers

abstract class FlowableUseCase<T>(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(): Flowable<T>

    fun source(): Flowable<T> = sourceImpl().compose(transformer())

    private fun transformer(): FlowableTransformer<T, T> =
        FlowableTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
