package com.orcchg.githubuser.domain.interactor.base

import com.orcchg.githubuser.domain.executor.UseCasePostExecutor
import com.orcchg.githubuser.domain.executor.UseCaseThreadExecutor
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import io.reactivex.schedulers.Schedulers

abstract class CompletableUseCase(threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : UseCase(threadExecutor, postExecutor) {

    protected abstract fun sourceImpl(): Completable

    fun source(): Completable = sourceImpl().compose(transformer())

    private fun transformer(): CompletableTransformer =
        CompletableTransformer {
            it.subscribeOn(Schedulers.from(threadExecutor))
              .observeOn(postExecutor.scheduler())
        }
}
