package com.ringoid.data.repository.image

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.exception.InvalidAccessTokenException
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.repository.image.IImageRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(private val local: ImageDao,
                                          cloud: RingoidCloud, spm: SharedPrefsManager
) : BaseRepository(cloud, spm), IImageRepository {

    // TODO: always check db first
    override fun getUserImages(resolution: String): Single<List<UserImage>> =
        spm.accessToken()?.let {
            cloud.getUserImages(accessToken = it, resolution = resolution)
                 .map { it.map() }
        } ?: Single.error<List<UserImage>> { InvalidAccessTokenException() }
}
