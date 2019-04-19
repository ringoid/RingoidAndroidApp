package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.messenger.IMessengerRepository
import io.reactivex.Single
import javax.inject.Inject

class GetMessagesForPeerUseCase @Inject constructor(private val repository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<List<Message>>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<List<Message>> {
        val chatId = params.get<String>("chatId")
        val sourceFeed = params.get<String>("sourceFeed") ?: DomainUtil.SOURCE_FEED_MESSAGES

        return chatId.takeIf { !it.isNullOrBlank() }
            ?.let { repository.getMessages(chatId = it, sourceFeed = sourceFeed) }
            ?: Single.error(MissingRequiredParamsException())
    }
}
