package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.report.exception.MissingRequiredParamsException
import io.reactivex.Single
import javax.inject.Inject

/**
 * Get [Chat] data for peerId, which is the opposite user (same as chatId in similar use-cases).
 */
class GetChatUseCase @Inject constructor(private val repository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Chat>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Chat> {
        val chatId = params.get<String>("chatId")
        val isChatOpen = params.get<Boolean>("isChatOpen") ?: false

        return if (chatId.isNullOrBlank()) {
            Single.error(MissingRequiredParamsException())
        } else {
            params.processSingle(ImageResolution::class.java) {
                repository.getChat(chatId = chatId, resolution = it, isChatOpen = isChatOpen)
            }
        }
    }
}
