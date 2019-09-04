package com.ringoid.domain.interactor.image

import android.net.Uri
import com.ringoid.report.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processCompletable
import com.ringoid.domain.model.essence.image.IImageUploadUrlEssence
import com.ringoid.domain.repository.image.IUserImageRepository
import io.reactivex.Completable
import javax.inject.Inject

class FailedCreateUserImageUseCase @Inject constructor(private val repository: IUserImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable {
        val imageFileUri = params.get<Uri>("uri")

        return imageFileUri?.path?.let { uriPath ->
            params.processCompletable(IImageUploadUrlEssence::class.java) {
                repository.createImageFail(essence = it, imageFilePath = uriPath)
            }
        } ?: Completable.error(MissingRequiredParamsException())
    }
}
