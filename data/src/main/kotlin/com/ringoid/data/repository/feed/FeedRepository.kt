package com.ringoid.data.repository.feed

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val local: FeedDao, cloud: RingoidCloud,
    spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IFeedRepository {

    // TODO: always check db first
    override fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed> =
        spm.accessSingle { cloud.getNewFaces(it.accessToken, resolution, limit, lastActionTime = aObjPool.lastActionTime)
            .map { it.map() } }
}
