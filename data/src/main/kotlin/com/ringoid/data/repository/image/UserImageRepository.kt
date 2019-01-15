package com.ringoid.data.repository.image

import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.image.IImageUploadUrlEssence
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssenceUnauthorized
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.image.LocalImage
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.image.IUserImageRepository
import com.ringoid.utility.uriString
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserImageRepository @Inject constructor(private val requestSet: ImageRequestSet,
    @Named("user") private val local: ImageDao, cloud: RingoidCloud, spm: ISharedPrefsManager)
    : BaseRepository(cloud, spm), IUserImageRepository {

    override val imageCreate: PublishSubject<String> = PublishSubject.create<String>()
    override val imageDelete: PublishSubject<String> = PublishSubject.create<String>()
    override val imageIdChange: PublishSubject<String> = PublishSubject.create<String>()
    override val imagesRefresh: PublishSubject<Int> = PublishSubject.create<Int>()

    // TODO: always check db first
    override fun getUserImages(resolution: ImageResolution): Single<List<UserImage>> =
        spm.accessSingle {
            cloud.getUserImages(it.accessToken, resolution)
                .handleError()  // TODO: on fail - notify
                .compose(requestSet.addCreatedImagesInResponse())
                .compose(requestSet.filterOutRemovedImages())
                .map { it.map { it as UserImage } }  // cast not losing fields in UserImage
                .doOnSuccess {
                    Timber.v("Loaded user images: now updating the whole local cache...")
                    local.apply {
                        deleteAllImages()  // refresh the whole cache with the remote cloud
                        addUserImages(it.map { UserImageDbo.from(it) })
                        imagesRefresh.onNext(it.size)  // notify database changed
                    }
                }
        }

    override fun deleteUserImage(essence: ImageDeleteEssence): Completable {
        val request = DeleteImageRequest(imageId = essence.imageId)
        return spm.accessSingle {
            cloud.deleteUserImage(essence)
                .doOnSubscribe {
                    // TODO: possibly old id
                    local.deleteImage(id = essence.imageId)
                    imageDelete.onNext(essence.imageId)  // notify database changed
                }
        }
        .doOnSubscribe { requestSet.remove(request) }
        .doOnSuccess { requestSet.fulfilled(request.id) }
        .handleError()  // TODO: on fail - notify and restore delete item in Db
        .ignoreElement()  // convert to Completable
    }

    // ------------------------------------------------------------------------
    override fun createImage(essence: IImageUploadUrlEssence, image: File): Single<Image> {
        val localImageRequest = CreateLocalImageRequest(image = LocalImage(file = image))
        return spm.accessSingle { accessToken ->
            val xessence = when (essence) {
                is ImageUploadUrlEssence -> essence  // for ImageUploadUrlEssence with access token supplied
                is ImageUploadUrlEssenceUnauthorized -> ImageUploadUrlEssence.from(essence, accessToken.accessToken)
                else -> throw IllegalArgumentException("Unsupported implementation of IImageUploadUrlEssence for createImage()")
            }

            cloud.getImageUploadUrl(xessence)
                .doOnSubscribe {
                    requestSet.create(localImageRequest)
                    local.addImage(ImageDbo(profileId = accessToken.userId, id = localImageRequest.id, uri = image.uriString()))
                    imageCreate.onNext(localImageRequest.id)  // notify database changed
                }
                .doOnSuccess { image ->
                    if (image.imageUri.isNullOrBlank()) {
                        throw NullPointerException("Upload uri is null: $image")
                    }
                    val request = CreateImageRequest(id = localImageRequest.id, image = image.map())
                    requestSet.create(request)  // rewrite local image request with remote image request

                    // replace local id with remote-generated id for local image in cache
                    local.deleteImage(id = localImageRequest.id)
                         .takeIf { it == 1 }
                         ?.let { local.addImage(ImageDbo(profileId = accessToken.userId, id = image.originImageId, uri = image.imageUri)) }
                    imageIdChange.onNext(image.originImageId)  // notify image id change in database
                }
                .flatMap {
                    cloud.uploadImage(url = it.imageUri!!, image = image)
                        .handleError()  // TODO: on fail - notify and delete added item in Db
                        .andThen(Single.just(it))
                        .map { it.map() }
                }
                .doOnSuccess { requestSet.fulfilled(localImageRequest.id) }
        }  // TODO: add request to 'requestSet', on subscribe
    }

    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessSingle { cloud.getImageUploadUrl(essence).map { it.map() } }

    override fun uploadImage(url: String, image: File): Completable =
        cloud.uploadImage(url = url, image = image)
}
