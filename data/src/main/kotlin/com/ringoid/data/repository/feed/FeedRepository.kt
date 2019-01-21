package com.ringoid.data.repository.feed

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.domain.model.mapList
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.feed.IFeedRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val local: FeedDao, @Named("user") private val cache: UserFeedDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IFeedRepository {

    override fun cacheBlockedProfileId(profileId: String): Completable =
        Completable.fromCallable { cache.addBlockedProfileId(ProfileIdDbo(profileId)) }

    override fun getBlockedProfileIds(): Single<List<String>> =
        cache.blockedProfileIds().map { it.mapList() }

    override fun deleteBlockedProfileIds(): Completable =
        Completable.fromCallable { cache.deleteBlockedProfileIds() }

    // ------------------------------------------
    // TODO: always check db first
    override fun getNewFaces(resolution: ImageResolution, limit: Int?): Single<Feed> =
        spm.accessSingle { cloud.getNewFaces(it.accessToken, resolution, limit, lastActionTime = aObjPool.lastActionTime)
            .map { it.map() } }

    override fun getLmm(resolution: ImageResolution): Single<Lmm> =
        spm.accessSingle { cloud.getLmm(it.accessToken, resolution, lastActionTime = aObjPool.lastActionTime)
            .map { it.map() }}
}
