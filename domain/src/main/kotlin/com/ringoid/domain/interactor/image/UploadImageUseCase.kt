package com.ringoid.domain.interactor.image

import com.ringoid.domain.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.image.IImageRepository
import io.reactivex.Completable
import java.io.File
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(private val repository: IImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable {
        val url = params.get<String>("url")
        val filename = params.get<String>("filename")

        if (url.isNullOrBlank() || filename.isNullOrBlank()) {
            throw MissingRequiredParamsException()
        }
        return repository.uploadImage(url = url, image = File(filename))
    }
}
