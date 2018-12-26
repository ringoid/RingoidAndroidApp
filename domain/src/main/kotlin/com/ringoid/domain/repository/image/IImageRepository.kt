package com.ringoid.domain.repository.image

import com.ringoid.domain.model.image.UserImage
import io.reactivex.Single

interface IImageRepository {

    fun getUserImages(resolution: String): Single<List<UserImage>>
}
