package com.ringoid.domain.interactor.image

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.interactor.base.processSingle
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.repository.image.IUserImageRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUserImagesUseCase @Inject constructor(val repository: IUserImageRepository,
    threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<List<UserImage>>(threadExecutor, postExecutor) {

    override fun sourceImpl(params: Params): Single<List<UserImage>> =
        params.processSingle(ImageResolution::class.java, repository::getUserImages)
}
