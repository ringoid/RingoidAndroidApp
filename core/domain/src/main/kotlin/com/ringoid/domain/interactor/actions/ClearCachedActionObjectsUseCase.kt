package com.ringoid.domain.interactor.actions

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processCompletable
import com.ringoid.domain.repository.actions.IActionObjectRepository
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Clears cached action objects, either all or for types specified.
 */
class ClearCachedActionObjectsUseCase @Inject constructor(private val repository: IActionObjectRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable =
        params.processCompletable("actionType", repository::deleteActionObjectsForType)
}
