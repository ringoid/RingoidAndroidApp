package com.ringoid.domain.interactor.image

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processCompletable
import com.ringoid.domain.model.essence.image.ImageDeleteEssenceUnauthorized
import com.ringoid.domain.repository.image.IUserImageRepository
import com.ringoid.utility.DebugOnly
import io.reactivex.Completable
import javax.inject.Inject

@DebugOnly
class FailedDeleteUserImageUseCase @Inject constructor(private val repository: IUserImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable =
        params.processCompletable(ImageDeleteEssenceUnauthorized::class.java, repository::deleteUserImageFail)
}
