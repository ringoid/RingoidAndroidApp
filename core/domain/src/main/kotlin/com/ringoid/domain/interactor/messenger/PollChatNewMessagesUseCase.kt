package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.FlowableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processFlowable
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.report.exception.MissingRequiredParamsException
import io.reactivex.Flowable
import javax.inject.Inject

class PollChatNewMessagesUseCase @Inject constructor(private val repository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : FlowableUseCase<Chat>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Flowable<Chat> {
        val chatId = params.get<String>("chatId")
        val isChatOpen = params.get<Boolean>("isChatOpen") ?: false

        return if (chatId.isNullOrBlank()) {
            Flowable.error(MissingRequiredParamsException())
        } else {
            params.processFlowable(ImageResolution::class.java) {
                repository.pollChatNew(chatId = chatId, resolution = it, isChatOpen = isChatOpen)
            }
        }
    }
}
