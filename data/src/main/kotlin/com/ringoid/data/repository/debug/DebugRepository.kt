package com.ringoid.data.repository.debug

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.shared_prefs.accessCompletable
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.DaggerCloudComponent
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.feed.IDebugRepository
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DebugRepository @Inject constructor(
    @Named("feed") private val imageLocal: ImageDao,
    @Named("user") private val userImageLocal: ImageDao,
    private val feedLocal: FeedDao, private val messageLocal: MessageDao,
    @Named("user") private val userLocal: UserDao,
    @Named("user") private val userFeedLocal: UserFeedDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IDebugRepository {

    override fun requestWithInvalidAccessToken(token: String): Completable =
        cloud.getUserImages(accessToken = token, resolution = ImageResolution._480x640)
            .handleError()
            .ignoreElement()  // convert to Completable

    override fun requestWithUnsupportedAppVersion(): Completable {
        val cloud = DaggerCloudComponent.builder()
            .cloudModule(CloudModule(BuildConfig.BUILD_NUMBER - 1))
            .ringoidCloudModule(RingoidCloudModule())
            .build()
            .cloud()
        return spm.accessCompletable {
            cloud.getNewFaces(it.accessToken, ImageResolution._480x640, 20, lastActionTime = aObjPool.lastActionTime)
                .handleError()
                .map { it.map() }
                .ignoreElement()  // convert to Completable
        }
    }

    override fun requestWithWrongParameters(): Completable {
        val essence = AuthCreateProfileEssence(yearOfBirth = 1930, sex = "shemale")
        return cloud.createUserProfile(essence)
            .handleError()
            .ignoreElement()  // convert to Completable
    }
}
