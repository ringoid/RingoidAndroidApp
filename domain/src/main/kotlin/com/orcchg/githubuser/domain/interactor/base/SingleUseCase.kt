package com.orcchg.githubuser.domain.interactor.base

import com.orcchg.githubuser.domain.executor.UseCasePostExecutor
import com.orcchg.githubuser.domain.executor.UseCaseThreadExecutor
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.schedulers.Schedulers

abstract class SingleUseCase<T>(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(): Single<T>

    fun source(): Single<T> = sourceImpl().compose(transformer())

    private fun transformer(): SingleTransformer<T, T> =
        SingleTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
