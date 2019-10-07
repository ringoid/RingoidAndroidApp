package com.ringoid.domain.interactor.actions

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.repository.actions.IActionObjectRepository
import com.ringoid.utility.DebugOnly
import io.reactivex.Single
import javax.inject.Inject

@DebugOnly
class CountActionObjectsCachedInPoolUseCase @Inject constructor(private val repository: IActionObjectRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Int>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Int> = repository.countCachedActionObjects()
}
