package com.ringoid.data.local.database.facade.image

import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import com.ringoid.datainterface.image.IImageRequestDbFacade
import com.ringoid.domain.model.image.ImageRequest
import com.ringoid.domain.model.mapList
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRequestDbFacadeImpl @Inject constructor(private val dao: ImageRequestDao) : IImageRequestDbFacade {

    override fun addRequest(request: ImageRequest) {
        ImageRequestDbo.from(request).also { dao.addRequest(it) }
    }

    override fun countRequests(): Single<Int> = dao.countRequests()

    override fun deleteAllRequests(): Int = dao.deleteAllRequests()

    override fun deleteRequest(request: ImageRequest) =
        dao.deleteRequest(ImageRequestDbo.from(request))

    override fun requests(): Single<List<ImageRequest>> =
        dao.requests().map { it.mapList() }
}
