package com.ringoid.data.repository.image

import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.exception.InvalidAccessTokenException
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.repository.image.IImageRepository
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val local: ImageDao, cloud: RingoidCloud, spm: SharedPrefsManager)
    : BaseRepository(cloud, spm), IImageRepository {

    // TODO: always check db first
    override fun getUserImages(resolution: String): Single<List<UserImage>> =
        spm.accessToken()?.let {
            cloud.getUserImages(accessToken = it.accessToken, resolution = resolution)
                 .map { it.map() }
        } ?: Single.error<List<UserImage>> { InvalidAccessTokenException() }

    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessToken()?.let {
            cloud.getImageUploadUrl(essence).map { it.map() }
        } ?: Single.error<Image> { InvalidAccessTokenException() }

    override fun uploadImage(url: String, image: File): Completable =
        cloud.uploadImage(url = url, image = image)
}
