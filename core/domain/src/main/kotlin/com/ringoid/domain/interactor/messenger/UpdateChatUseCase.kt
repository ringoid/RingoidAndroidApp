package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.repository.feed.IFeedRepository
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.report.exception.MissingRequiredParamsException
import io.reactivex.Single
import javax.inject.Inject

/**
 * Some Chat has been updated with the incoming push notification. That push
 * notification is considered as another source of Chat data, which is part of LC data.
 * Since any update of LC data could have side-effects, here the implementation
 * is being notified also that the update has occurred and it then should perform
 * handling of any side-effects those update might internally involve.
 */
class UpdateChatUseCase @Inject constructor(
    private val feedRepository: IFeedRepository,
    private val messengerRepository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Chat>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Chat> {
        val chatId = params.get<String>("chatId")
        val isChatOpen = params.get<Boolean>("isChatOpen") ?: false

        return if (chatId.isNullOrBlank()) {
            Single.error(MissingRequiredParamsException())
        } else {
            params.processSingle(ImageResolution::class.java) {
                messengerRepository.updateChat(chatId = chatId, resolution = it, isChatOpen = isChatOpen)
                    .map { (chat, isInserted) ->
                        if (isInserted) {
                            feedRepository.onUpdateSomeChatExternal()
                        }
                        chat
                    }
            }
        }
    }
}
