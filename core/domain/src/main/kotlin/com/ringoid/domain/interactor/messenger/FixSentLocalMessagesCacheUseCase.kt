package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.report.exception.MissingRequiredParamsException
import io.reactivex.Completable
import javax.inject.Inject

class FixSentLocalMessagesCacheUseCase @Inject constructor(private val repository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable {
        val chatId = params.get<String>("chatId")
        val unconsumedClientIds = params.get<List<String>>("unconsumedClientIds") ?: emptyList()

        return if (chatId.isNullOrBlank()) {
            Completable.error(MissingRequiredParamsException())
        } else {
            repository.fixSentLocalMessagesCache(chatId, unconsumedClientIds)
        }
    }
}
