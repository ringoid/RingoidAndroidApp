package com.ringoid.domain.interactor.image

import com.ringoid.report.exception.MissingRequiredParamsException
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.repository.image.IUserImageRepository
import io.reactivex.Completable
import java.io.File
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(private val repository: IUserImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : CompletableUseCase(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Completable {
        val url = params.get<String>("url")
        val filename = params.get<String>("filename")

        return if (url.isNullOrBlank() || filename.isNullOrBlank()) {
            Completable.error(MissingRequiredParamsException())
        } else {
            repository.uploadImage(url = url.orEmpty(), image = File(filename))
        }
    }
}
