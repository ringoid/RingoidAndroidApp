package com.ringoid.data.repository.image

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.image.ImageDao
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
    @Named("user") private val local: ImageDao, cloud: RingoidCloud,
    spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IUserImageRepository {

    override val imageCreate = PublishSubject.create<String>()
    override val imageDelete = PublishSubject.create<String>()
    override val imageIdChange = PublishSubject.create<Pair<String, String>>()

    override fun countUserImages(): Single<Int> = local.countUserImages()

    override fun getUserImage(id: String): Single<UserImage> = local.userImage(id).map { it.map() }

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
                        // though database has changed, result is used upstream
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
        .handleError()  // TODO: on fail - notify and restore delete item in Db
        .doOnSuccess { requestSet.fulfilled(request.id) }
        .ignoreElement()  // convert to Completable
    }

    // ------------------------------------------------------------------------
    override fun createImage(essence: IImageUploadUrlEssence, image: File): Single<Image> {
        val localImageRequest = CreateLocalImageRequest(id = essence.clientImageId, image = LocalImage(file = image))
        return spm.accessSingle { accessToken ->
            val xessence = when (essence) {
                is ImageUploadUrlEssence -> essence  // for ImageUploadUrlEssence with access token supplied
                is ImageUploadUrlEssenceUnauthorized -> ImageUploadUrlEssence.from(essence, accessToken.accessToken)
                else -> throw IllegalArgumentException("Unsupported implementation of IImageUploadUrlEssence for createImage()")
            }

            cloud.getImageUploadUrl(xessence)
                .doOnSubscribe {
                    requestSet.create(localImageRequest)
                    local.addImage(UserImageDbo(id = xessence.clientImageId, uri = image.uriString(), originId = xessence.clientImageId, isBlocked = false))
                    imageCreate.onNext(xessence.clientImageId)  // notify database changed
                }
                .doOnSuccess { image ->
                    if (image.imageUri.isNullOrBlank()) {
                        throw NullPointerException("Upload uri is null: $image")
                    }
                    val request = CreateImageRequest(id = localImageRequest.id, image = image.map())
                    requestSet.create(request)  // rewrite local image request with remote image request

                    // replace local id with remote-generated id for local image in cache
                    local.deleteImage(id = image.clientImageId)
                         .takeIf { it == 1 }
                         ?.let { local.addImage(UserImageDbo(id = image.originImageId, uri = image.imageUri, originId = image.originImageId, isBlocked = false)) }
                    imageIdChange.onNext(image.clientImageId to image.originImageId)  // notify image id change in database
                }
                // TODO: on fail getImageUploadUrl
                .flatMap {
                    cloud.uploadImage(url = it.imageUri!!, image = image)
                        .handleError()  // TODO: on fail - notify and delete added item in Db
                        .andThen(Single.just(it))
                        .map { it.map() }
                }
                .doOnSuccess { requestSet.fulfilled(localImageRequest.id) }
        }
    }

    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessSingle { cloud.getImageUploadUrl(essence).map { it.map() } }

    override fun uploadImage(url: String, image: File): Completable =
        cloud.uploadImage(url = url, image = image)
}
