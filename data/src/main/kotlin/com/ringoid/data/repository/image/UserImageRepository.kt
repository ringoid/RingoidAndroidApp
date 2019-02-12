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
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.model.mapList
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.image.IUserImageRepository
import com.ringoid.utility.uriString
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserImageRepository @Inject constructor(
    @Named("user") private val local: ImageDao, cloud: RingoidCloud,
    spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IUserImageRepository {

    override val imageCreate = PublishSubject.create<String>()
    override val imageDelete = PublishSubject.create<String>()
    override val totalUserImages = PublishSubject.create<Int>()

    override fun countUserImages(): Single<Int> = local.countUserImages()

    override fun getUserImage(id: String): Single<UserImage> = local.userImage(id).map { it.map() }

    override fun getUserImages(resolution: ImageResolution): Single<List<UserImage>> =
        spm.accessSingle { cloud.getUserImages(it.accessToken, resolution) }
            .handleError()
            .flatMap {
                Observable.fromIterable(it.images)
                    .doOnNext { local.updateUserImageByOriginId(originImageId = it.originId, uri = it.uri, isBlocked = it.isBlocked, numberOfLikes = it.numberOfLikes) }
                    .toList()
            }
            .flatMap { local.userImages() }
            .map { it.mapList() }

    override fun deleteUserImage(essence: ImageDeleteEssence): Completable =
        local.userImage(id = essence.imageId)
            .flatMapCompletable { localImage ->
                spm.accessSingle { cloud.deleteUserImage(essence.copyWith(imageId = localImage.originId)) }
                    .handleError()  // TODO: on fail - retry on pull-to-refresh
                    .doOnSubscribe {
                        local.deleteImage(id = essence.imageId)
                        imageDelete.onNext(essence.imageId)  // notify database changed
                    }
                    .ignoreElement()  // convert to Completable
            }

    override fun deleteLocalUserImages(): Completable = Completable.fromCallable { local.deleteAllImages() }

    // ------------------------------------------------------------------------
    override fun createImage(essence: IImageUploadUrlEssence, image: File): Single<Image> =
        spm.accessSingle { accessToken ->
            val xessence = when (essence) {
                is ImageUploadUrlEssence -> essence  // for ImageUploadUrlEssence with access token supplied
                is ImageUploadUrlEssenceUnauthorized -> ImageUploadUrlEssence.from(essence, accessToken.accessToken)
                else -> throw IllegalArgumentException("Unsupported implementation of IImageUploadUrlEssence for createImage()")
            }

            cloud.getImageUploadUrl(xessence)
                .handleError()  // TODO: on fail - retry on pull-to-refresh
                .doOnSubscribe {
                    val localImage = UserImageDbo(id = xessence.clientImageId, uri = image.uriString())
                    local.addImage(localImage)
                    imageCreate.onNext(xessence.clientImageId)  // notify database changed
                }
                .doOnSuccess { image ->
                    if (image.imageUri.isNullOrBlank()) {
                        throw NullPointerException("Upload uri is null: $image")
                    }
                    /**
                     * Update [UserImageDbo.originId] and [UserImageDbo.uri] with remote-generated id and uri
                     * for image in local cache.
                     */
                    val updatedLocalImage = UserImageDbo(originId = image.originImageId, id = xessence.clientImageId, uri = image.imageUri)
                    local.updateUserImage(updatedLocalImage)  // local image now has proper originId and remote url
                }
                .flatMap {
                    cloud.uploadImage(url = it.imageUri!!, image = image)
                        .handleError()  // TODO: on fail - retry on pull-to-refresh
                        .andThen(Single.just(it))
                        .map { it.map() }
                }
        }

    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessSingle { cloud.getImageUploadUrl(essence).map { it.map() } }

    override fun uploadImage(url: String, image: File): Completable =
        cloud.uploadImage(url = url, image = image)

    // --------------------------------------------------------------------------------------------
    override fun fulfillPendingImageRequests(): Completable =
        Completable.fromCallable {  }  // TODO: fulfill all requests
}
