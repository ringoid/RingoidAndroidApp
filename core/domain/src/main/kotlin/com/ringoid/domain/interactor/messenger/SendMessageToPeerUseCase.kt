package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.messenger.IMessengerRepository
import io.reactivex.Single
import javax.inject.Inject

class SendMessageToPeerUseCase @Inject constructor(private val repository: IMessengerRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Message>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Message> =
        params.processSingle(MessageEssence::class.java, repository::sendMessage)
}
