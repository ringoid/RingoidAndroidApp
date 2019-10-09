package com.ringoid.domain.interactor.user

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.actions.ClearCachedActionObjectsUseCase
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.*
import com.ringoid.domain.interactor.image.ClearCachedImageRequestsUseCase
import com.ringoid.domain.interactor.image.ClearCachedUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesUseCase
import com.ringoid.domain.model.actions.ActionObject
import com.ringoid.domain.repository.user.IUserRepository
import io.reactivex.Completable
import javax.inject.Inject

class DoOnLogoutUseCase @Inject constructor(
    private val repository: IUserRepository,
    private val clearCachedActionObjectsUseCase: ClearCachedActionObjectsUseCase,
    private val clearLocalUserDataUseCase: ClearLocalUserDataUseCase,
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    private val clearCachedBlockedProfileIdsUseCase: ClearCachedBlockedProfileIdsUseCase,
    private val clearCachedLmmUseCase: ClearCachedLmmUseCase,
    private val clearCachedLmmProfileIdsUseCase: ClearCachedLmmProfileIdsUseCase,
    private val clearCachedLmmTotalCountsUseCase: ClearCachedLmmTotalCountsUseCase,
    private val clearCachedUserImagesUseCase: ClearCachedUserImagesUseCase,
    private val clearCachedImageRequestsUseCase: ClearCachedImageRequestsUseCase,
    private val clearMessagesUseCase: ClearMessagesUseCase,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable =
        repository.doOnLogout()
            .andThen(clearLocalUserDataUseCase.source())
            .andThen(clearCachedAlreadySeenProfileIdsUseCase.source())
            .andThen(clearCachedBlockedProfileIdsUseCase.source())
            .andThen(clearCachedLmmUseCase.source())
            .andThen(clearCachedLmmProfileIdsUseCase.source())
            .andThen(clearCachedLmmTotalCountsUseCase.source())
            .andThen(clearCachedUserImagesUseCase.source())
            .andThen(clearCachedImageRequestsUseCase.source())
            .andThen(clearMessagesUseCase.source())
            .andThen(clearCachedActionObjectsUseCase.source(params = Params().put("actionType", ActionObject.ACTION_TYPE_MESSAGE_READ)))
}
