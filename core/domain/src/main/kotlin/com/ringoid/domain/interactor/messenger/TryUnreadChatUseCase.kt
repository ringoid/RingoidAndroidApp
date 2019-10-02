package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Checks whether chat specified with 'chatId' is a new unread by user one, and if so,
 * that 'chatId' will be cached and some corresponding side-effects involved.
 */
class TryUnreadChatUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Boolean>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Boolean> =
        params.processSingle("chatId", repository::tryUnreadChat)
}
