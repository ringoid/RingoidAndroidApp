package com.ringoid.domain.repository.feed

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Lmm
import io.reactivex.Single

interface IFeedRepository {

    fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed>

    fun getLmm(resolution: ImageResolution): Single<Lmm>
}
