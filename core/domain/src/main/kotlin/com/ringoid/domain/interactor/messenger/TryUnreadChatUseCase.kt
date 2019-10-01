package com.ringoid.domain.interactor.messenger

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processCompletable
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Checks whether chat specified with 'chatId' is a new unread by user one, and if so,
 * that 'chatId' will be cached and some corresponding side-effects involved.
 */
class TryUnreadChatUseCase @Inject constructor(private val repository: IFeedRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable =
        params.processCompletable("chatId", repository::tryUnreadChat)
}
