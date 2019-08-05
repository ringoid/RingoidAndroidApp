package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.repository.messenger.IMessengerRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Get [Chat] data for peerId, which is the opposite user (same as chatId in similar use-cases).
 * To the contrast of [GetChatUseCase], this use case does not trigger action objects' Queue.
 */
class GetChatOnlyUseCase @Inject constructor(private val repository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Chat>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Chat> {
        val chatId = params.get<String>("chatId")

        return if (chatId.isNullOrBlank()) {
            Single.error(MissingRequiredParamsException())
        } else {
            params.processSingle(ImageResolution::class.java) {
                repository.getChatOnly(chatId = chatId, resolution = it)
            }
        }
    }
}
