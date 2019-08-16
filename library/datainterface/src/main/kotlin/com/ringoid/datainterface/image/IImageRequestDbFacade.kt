package com.ringoid.datainterface.image

import com.ringoid.domain.model.image.ImageRequest
import io.reactivex.Single

interface IImageRequestDbFacade {

    fun addRequest(request: ImageRequest)

    fun countRequests(): Single<Int>

    fun deleteAllRequests(): Int

    fun deleteRequest(request: ImageRequest)

    fun requests(): Single<List<ImageRequest>>
}
