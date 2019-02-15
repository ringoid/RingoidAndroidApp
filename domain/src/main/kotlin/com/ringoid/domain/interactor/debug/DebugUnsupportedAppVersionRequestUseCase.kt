package com.ringoid.domain.interactor.debug

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.misc.DebugOnly
import com.ringoid.domain.repository.debug.IDebugRepository
import io.reactivex.Completable
import javax.inject.Inject

@DebugOnly
class DebugUnsupportedAppVersionRequestUseCase @Inject constructor(val repository: IDebugRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable = repository.debugRequestWithUnsupportedAppVersion()
}
