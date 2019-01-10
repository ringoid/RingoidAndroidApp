package com.ringoid.data.repository.image

import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.image.LocalImage
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.image.IImageRepository
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(private val requestSet: ImageRequestSet,
    private val local: ImageDao, cloud: RingoidCloud, spm: ISharedPrefsManager)
    : BaseRepository(cloud, spm), IImageRepository {

    // TODO: always check db first
    override fun getUserImages(resolution: String): Single<List<UserImage>> =
        spm.accessSingle {
            cloud.getUserImages(it.accessToken, resolution)
                .compose(requestSet.addCreatedImagesInResponse())
                .compose(requestSet.filterOutRemovedImages())
                .map { it.map { it as UserImage } }  // cast not losing fields in UserImage
        }

    override fun deleteUserImage(essence: ImageDeleteEssence): Completable {
        val request = DeleteImageRequest(imageId = essence.imageId)
        return spm.accessSingle { cloud.deleteUserImage(essence) }
            .doOnSubscribe { requestSet.remove(request) }
            .doOnSuccess { requestSet.fulfilled(request.id) }
            .handleError()
            .ignoreElement()  // convert to Completable
    }

    // ------------------------------------------------------------------------
    override fun createImage(essence: ImageUploadUrlEssence, image: File): Single<Image> {
        val localImageRequest = CreateLocalImageRequest(image = LocalImage(file = image))
        return spm.accessSingle {
            cloud.getImageUploadUrl(essence)
                .doOnSubscribe { requestSet.create(localImageRequest) }
                .doOnSuccess {
                    if (it.imageUri.isBlank()) {
                        throw NullPointerException("Upload uri is null: $it")
                    }
                    val request = CreateImageRequest(id = localImageRequest.id, image = it.map())
                    requestSet.create(request)  // rewrite local image request with remote image request
                }
                .flatMap {
                    cloud.uploadImage(url = it.imageUri, image = image)
                        .andThen(Single.just(it))
                        .handleError()
                        .map { it.map() }
                }
                .doOnSuccess { requestSet.fulfilled(localImageRequest.id) }
        }  // TODO: add request to set on subscribe
    }

    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessSingle { cloud.getImageUploadUrl(essence).map { it.map() } }

    override fun uploadImage(url: String, image: File): Completable =
        cloud.uploadImage(url = url, image = image)
}
