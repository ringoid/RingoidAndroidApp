package com.ringoid.domain.interactor.image

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.repository.image.IImageRepository
import io.reactivex.Single
import javax.inject.Inject

class GetImageUploadUrlUseCase @Inject constructor(private val repository: IImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Image>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Image> =
        params.processSingle(ImageUploadUrlEssence::class.java, repository::getImageUploadUrl)
}
