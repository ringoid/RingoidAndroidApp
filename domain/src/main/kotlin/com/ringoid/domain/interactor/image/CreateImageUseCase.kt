package com.ringoid.domain.interactor.image

import android.net.Uri
import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.repository.image.IImageRepository
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class CreateImageUseCase @Inject constructor(private val repository: IImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<Image>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<Image> {
        val image = params.get<Uri>("uri")?.let { File(it.path) } ?: throw MissingRequiredParamsException()

        return params.processSingle(ImageUploadUrlEssence::class.java) {
            repository.getImageUploadUrl(it)
                .doOnSuccess { if (it.uri.isNullOrBlank()) throw NullPointerException("Upload uri is null: $it") }
                .flatMap { repository.uploadImage(it.uri!!, image).andThen(Single.just(it)) }
        }
    }
}
