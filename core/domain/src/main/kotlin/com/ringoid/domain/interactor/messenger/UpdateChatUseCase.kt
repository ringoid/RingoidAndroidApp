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
 * Some Chat has been updated with the incoming push notification. That push
 * notification is considered as another source of Chat data, which is part of LC data.
 * Since any update of LC data could have side-effects, here the implementation
 * is being notified also that the update has occurred and it then should perform
 * handling of any side-effects those update might internally involve.
 *
 * Result contains updated Chat (or empty in case of failure) and flag indicating that
 * chat is new unread by user, so some side-effects should be involved.
 * Chat is considered new unread by user, if it hasn't appear before, or it had but has been
 * read by user before and then became unread by user after successful update.
 */
class UpdateChatUseCase @Inject constructor(private val messengerRepository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Pair<Chat, Boolean>>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Pair<Chat, Boolean>> {
        val chatId = params.get<String>("chatId")
        val isChatOpen = params.get<Boolean>("isChatOpen") ?: false

        return if (chatId.isNullOrBlank()) {
            Single.error(MissingRequiredParamsException())
        } else {
            params.processSingle(ImageResolution::class.java) {
                messengerRepository.updateChat(chatId = chatId, resolution = it, isChatOpen = isChatOpen)
            }
        }
    }
}
