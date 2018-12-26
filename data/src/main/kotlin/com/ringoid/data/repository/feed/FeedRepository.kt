package com.ringoid.data.repository.feed

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.exception.InvalidAccessTokenException
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(private val local: FeedDao,
                                         cloud: RingoidCloud, spm: SharedPrefsManager
) : BaseRepository(cloud, spm), IFeedRepository {

    // TODO: always check db first
    override fun getNewFaces(resolution: String, limit: Int): Single<Feed> =
        spm.accessToken()?.let {
            cloud.getNewFaces(accessToken = it, resolution = resolution, limit = limit)
                 .map { it.map() }
        } ?: Single.error<Feed> { InvalidAccessTokenException() }
}
