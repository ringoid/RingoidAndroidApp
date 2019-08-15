package com.ringoid.domain.interactor.image

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.ObservableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.processObservable
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.repository.image.IUserImageRepository
import io.reactivex.Observable
import javax.inject.Inject

class GetUserImagesAsyncUseCase @Inject constructor(private val repository: IUserImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : ObservableUseCase<List<UserImage>>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Observable<List<UserImage>> =
        params.processObservable(ImageResolution::class.java, repository::getUserImagesAsync)
}
