package com.ringoid.domain.interactor.system

import com.ringoid.report.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.system.ISystemRepository
import io.reactivex.Completable
import javax.inject.Inject

class PostToSlackUseCase @Inject constructor(private val repository: ISystemRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable {
        val channelId = params.get<String>("channelId")
        val text = params.get<String>("text") ?: ""

        return if (channelId.isNullOrBlank()) {
            Completable.error(MissingRequiredParamsException())
        } else {
            repository.postToSlack(channelId = channelId, text = text)
        }
    }
}
