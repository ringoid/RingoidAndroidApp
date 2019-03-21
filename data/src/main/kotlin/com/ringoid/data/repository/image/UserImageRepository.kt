package com.ringoid.data.repository.image

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.shared_prefs.accessCompletable
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.image.UserImageEntity
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.exception.isFatalApiError
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.image.*
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.model.mapList
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.image.IUserImageRepository
import com.ringoid.utility.uriString
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserImageRepository @Inject constructor(
    @Named("user") private val local: ImageDao,
    @Named("user") private val imageRequestLocal: ImageRequestDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IUserImageRepository {

    override val imageBlocked = PublishSubject.create<String>()
    override val imageCreated = PublishSubject.create<String>()
    override val imageDeleted = PublishSubject.create<String>()
    override val totalUserImages = PublishSubject.create<Int>()

    override fun countUserImages(): Single<Int> =
        local.countUserImages().doOnSuccess { totalUserImages.onNext(it) }

    override fun getUserImage(id: String): Single<UserImage> = local.userImage(id).map { it.map() }

    override fun getUserImages(resolution: ImageResolution): Single<List<UserImage>> {
        fun fetchUserImages(): Single<List<UserImage>> =
            spm.accessSingle { cloud.getUserImages(it.accessToken, resolution) }
                .handleError()
                .flatMap {
                    Observable.fromIterable(it.images)  // images fetched from the Server
                        .zipWith(Observable.range(0, it.images.size), BiFunction { image: UserImageEntity, index: Int -> image to index })
                        .doOnNext { (image, index) ->
                            if (image.isBlocked && image.isBlocked != local.isUserImageBlockedByOriginId(originImageId = image.originId)) {
                                Timber.v("Image with id [${image.originId}] has been blocked by Moderator on the Server")
                                imageBlocked.onNext(image.originId)  // notify image has been blocked by Moderator
                            }
                            local.updateUserImageByOriginId(originImageId = image.originId, uri = image.uri,
                                numberOfLikes = image.numberOfLikes, isBlocked = image.isBlocked, sortPosition = index)
                        }
                        .toList()
                }
                .onErrorResumeNext {
                    if (it.isFatalApiError()) {
                        Single.error(it)
                    } else {
                        Single.just(emptyList())
                    }
                }
                .flatMap { local.userImages() }
                .map { it.mapList() }

        return imageRequestLocal.countRequests()
            .flatMap { count ->
                Timber.v("Total count of failed image requests: $count")
                if (count > 0) {
                    imageRequestLocal.requests()
                        .flatMap {
                            Observable.fromIterable(it)
                                .map { request ->
                                    when (request.type) {
                                        ImageRequestDbo.TYPE_CREATE -> {
                                            Timber.v("Execute 'create image' request again, as it's failed before")
                                            createImage(request.createRequestEssence(), request.imageFilePath, retryCount = 0)
                                                .ignoreElement()
                                        }
                                        ImageRequestDbo.TYPE_DELETE -> {
                                            Timber.v("Execute 'delete image' request again, as it's failed before")
                                            deleteUserImage(request.deleteRequestEssence(), retryCount = 0)
                                        }
                                        else -> Completable.complete()  // ignored item
                                    }
                                    .doOnComplete { imageRequestLocal.deleteRequest(request) }
                                }
                                .toList()
                        }
                        .flatMap {
                            Completable.concat(it)
                                .onErrorComplete()  // if any request fails, fetch user images whatever it is on the Server
                                .toSingle { 0L }
                        }
                        .flatMap { fetchUserImages().doOnSubscribe { Timber.v("Fetch actualized list of images") } }
                } else {
                    fetchUserImages().doOnSubscribe { Timber.v("Fetch list of images") }
                }
            }
    }

    override fun getUserImagesAsync(resolution: ImageResolution): Observable<List<UserImage>> =
        Observable.concatArrayEager(local.userImages().map { it.mapList() }.toObservable(), getUserImages(resolution).toObservable())

    override fun deleteUserImage(essence: ImageDeleteEssenceUnauthorized): Completable =
        deleteUserImage(essence, retryCount = BuildConfig.DEFAULT_RETRY_COUNT)

    private fun deleteUserImage(essence: ImageDeleteEssenceUnauthorized, retryCount: Int): Completable =
        spm.accessCompletable { deleteUserImage(ImageDeleteEssence.from(essence, it.accessToken), retryCount) }

    private fun deleteUserImage(essence: ImageDeleteEssence, retryCount: Int): Completable =
        local.userImage(id = essence.imageId)
            .flatMapCompletable { localImage ->
                spm.accessSingle { cloud.deleteUserImage(essence.copyWith(imageId = localImage.originId)) }
                    .handleError(count = retryCount)
                    .doOnError { imageRequestLocal.addRequest(ImageRequestDbo.from(essence)) }
                    .doOnSubscribe {
                        local.deleteImage(id = essence.imageId)
                        imageDeleted.onNext(essence.imageId)  // notify database changed
                    }
                    .ignoreElement()  // convert to Completable
            }

    override fun deleteLocalUserImages(): Completable = Completable.fromCallable { local.deleteAllImages() }

    override fun deleteLocalUserImageRequests(): Completable =
        Completable.fromCallable { imageRequestLocal.deleteAllRequests() }

    // ------------------------------------------------------------------------
    override fun createImage(essence: IImageUploadUrlEssence, imageFilePath: String): Single<Image> =
        createImage(essence, imageFilePath, retryCount = BuildConfig.DEFAULT_RETRY_COUNT)

    private fun createImage(essence: IImageUploadUrlEssence, imageFilePath: String, retryCount: Int): Single<Image> =
        spm.accessSingle { accessToken ->
            val xessence = when (essence) {
                is ImageUploadUrlEssence -> essence  // for ImageUploadUrlEssence with access token supplied
                is ImageUploadUrlEssenceUnauthorized -> ImageUploadUrlEssence.from(essence, accessToken.accessToken)
                else -> throw IllegalArgumentException("Unsupported implementation of IImageUploadUrlEssence for createImage()")
            }

            val imageFile = File(imageFilePath)
            val uriLocal = imageFile.uriString()
            val localImage = UserImageDbo(id = xessence.clientImageId, uri = uriLocal, uriLocal = uriLocal)

            cloud.getImageUploadUrl(xessence)
                .doOnSubscribe {
                    local.addImage(localImage)
                    imageCreated.onNext(xessence.clientImageId)  // notify database changed
                }
                .doOnSuccess { image ->
                    if (image.imageUri.isNullOrBlank()) {
                        throw NullPointerException("Upload uri is null: $image")
                    }
                    /**
                     * Update [UserImageDbo.originId] and [UserImageDbo.uri] with remote-generated id and uri
                     * for image in local cache, keeping [UserImageDbo.id] and [UserImageDbo.uriLocal] unchanged.
                     */
                    val updatedLocalImage = localImage.copyWith(originId = image.originImageId, uri = image.imageUri)
                    local.updateUserImage(updatedLocalImage)  // local image now has proper originId and remote url
                }
                .flatMap { cloud.uploadImage(url = it.imageUri!!, image = imageFile).andThen(Single.just(it)) }
                .handleError(count = retryCount)
                .doOnError { imageRequestLocal.addRequest(ImageRequestDbo.from(xessence, imageFilePath)) }
                .map { it.map() }
        }

    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessSingle { cloud.getImageUploadUrl(essence).map { it.map() } }

    override fun uploadImage(url: String, image: File): Completable = cloud.uploadImage(url = url, image = image)
}
