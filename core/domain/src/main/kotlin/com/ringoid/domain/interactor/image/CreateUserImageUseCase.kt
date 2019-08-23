package com.ringoid.domain.interactor.image

import android.net.Uri
import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.model.essence.image.IImageUploadUrlEssence
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.repository.image.IUserImageRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateUserImageUseCase @Inject constructor(val repository: IUserImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Image>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Image> {
        val imageFileUri = params.get<Uri>("uri")

        return imageFileUri?.path?.let { uriPath ->
            params.processSingle(IImageUploadUrlEssence::class.java) {
                repository.createImage(essence = it, imageFilePath = uriPath)
            }
        } ?: Single.error(MissingRequiredParamsException())
    }
}
