package com.ringoid.repository.image

import com.ringoid.data.handleError
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.shared_prefs.accessCompletable
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.di.PerBackup
import com.ringoid.datainterface.di.PerUser
import com.ringoid.datainterface.local.image.IImageRequestDbFacade
import com.ringoid.datainterface.local.user.IUserImageDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.model.image.UserImageEntity
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.image.*
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.image.ImageRequest
import com.ringoid.domain.model.image.UserImage
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.repository.image.IUserImageRepository
import com.ringoid.report.exception.SimulatedException
import com.ringoid.report.exception.isFatalApiError
import com.ringoid.report.log.Report
import com.ringoid.repository.BaseRepository
import com.ringoid.repository.UserInMemoryCache
import com.ringoid.utility.DebugOnly
import com.ringoid.utility.uriString
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserImageRepository @Inject constructor(
    @PerUser private val local: IUserImageDbFacade,
    @PerUser private val imageRequestLocal: IImageRequestDbFacade,
    @PerBackup private val imagesBackup: IUserImageDbFacade,
    private val userInMemoryCache: UserInMemoryCache,
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IUserImageRepository {

    private val imageBlocked = PublishSubject.create<String>()
    private val imageCreated = PublishSubject.create<String>()
    private val imageDeleted = PublishSubject.create<String>()
    private val totalUserImages = PublishSubject.create<Int>()
    override fun imageBlockedSource(): Observable<String> = imageBlocked.hide()
    override fun imageCreatedSource(): Observable<String> = imageCreated.hide()
    override fun imageDeletedSource(): Observable<String> = imageDeleted.hide()
    override fun totalUserImagesSource(): Observable<Int> = totalUserImages.hide()

    override fun countUserImages(): Single<Int> =
        local.countUserImages()
             .doOnSuccess {
                 userInMemoryCache.setUserImagesCount(it)
                 totalUserImages.onNext(it)
             }

    // ------------------------------------------------------------------------
    override fun getUserImage(id: String): Single<UserImage> = local.userImage(id)

    override fun getUserImages(resolution: ImageResolution): Single<List<UserImage>> {
        fun fetchUserImages(): Single<List<UserImage>> =
            spm.accessSingle { cloud.getUserImages(it.accessToken, resolution) }
                .handleError(tag = "getUserImages($resolution)", traceTag = "image/get_own_photos")
                .flatMap {
                    Observable.fromIterable(it.images)  // images fetched from the Server
                        .zipWith(Observable.range(0, it.images.size), BiFunction { image: UserImageEntity, index: Int -> image to index })
                        .doOnNext { (image, index) ->
                            if (image.isBlocked && image.isBlocked != local.isUserImageBlockedByOriginId(originImageId = image.originId)) {
                                Timber.v("Image with id [${image.originId}] has been blocked by Moderator on the Server")
                                imageBlocked.onNext(image.originId)  // notify image has been blocked by Moderator
                            }
                            local.updateUserImageByOriginId(
                                originImageId = image.originId,
                                uri = image.uri,
                                numberOfLikes = image.numberOfLikes,
                                isBlocked = image.isBlocked,
                                sortPosition = index)
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
                .flatMap { countUserImages() }  // actualize user images count
                .flatMap { local.userImages() }

        return imageRequestLocal.countRequests()
            .flatMap { count ->
                DebugLogUtil.i("Total count of failed image requests: $count")
                if (count > 0) {
                    imageRequestLocal.requests()
                        .flatMap {
                            DebugLogUtil.v("Pending image requests [${it.size}]: ${it.joinToString("\n\t\t", "\n\t\t", "\n", transform = { it.type })}")
                            Observable.fromIterable(it)
                                .map { request ->
                                    when (request.type) {
                                        ImageRequest.TYPE_CREATE -> {
                                            DebugLogUtil.i("Execute 'create image' request again, as it's failed before")
                                            createImageRemote(request.createRequestEssence(), request.imageFilePath, retryCount = 0)
                                                .ignoreElement()
                                        }
                                        ImageRequest.TYPE_DELETE -> {
                                            DebugLogUtil.i("Execute 'delete image' request again, as it's failed before")
                                            deleteUserImageRemote(request.deleteRequestEssence(), retryCount = 0)
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
        Observable.concatArray(local.userImages().toObservable(), getUserImages(resolution).toObservable())

    // ------------------------------------------------------------------------
    override fun deleteUserImage(essence: ImageDeleteEssenceUnauthorized): Completable =
        deleteUserImage(essence, retryCount = BuildConfig.DEFAULT_RETRY_COUNT)

    /**
     * Performs deleting image locally and then fails to delete image on remote.
     */
    @DebugOnly
    override fun deleteUserImageFail(essence: ImageDeleteEssenceUnauthorized): Completable =
        spm.accessCompletable {
            val xessence = ImageDeleteEssence.from(essence, it.accessToken)
            deleteUserImageLocal(xessence)
                .flatMapCompletable { Completable.error(SimulatedException()) }
                .doOnError {
                    Completable.fromCallable { imageRequestLocal.addRequest(ImageRequest.from(xessence)) }
                               .subscribeOn(Schedulers.io())
                               .subscribe({}, Timber::e)
                }
        }

    private fun deleteUserImage(essence: ImageDeleteEssenceUnauthorized, retryCount: Int): Completable =
        spm.accessCompletable { deleteUserImageAsync(ImageDeleteEssence.from(essence, it.accessToken), retryCount) }

    /**
     * Perform deleting image locally and then remotely. If remote delete has failed,
     * record that as pending request to repeat later, on next [getUserImages] call.
     */
    private fun deleteUserImageAsync(essence: ImageDeleteEssence, retryCount: Int): Completable =
        deleteUserImageLocal(essence)
            .flatMapCompletable { localImage ->
                deleteUserImageRemoteImpl(localImage, essence, retryCount)
            }

    private fun deleteUserImageLocal(essence: ImageDeleteEssence): Single<UserImage> =
        local.userImage(id = essence.imageId)
            .flatMap { localImage ->
                Completable.fromCallable { imagesBackup.addUserImage(localImage) }
                           .toSingleDefault(localImage)
            }
            .flatMap { localImage ->
                Single.fromCallable { local.deleteUserImage(id = essence.imageId) }
                    .doOnSuccess { imageDeleted.onNext(essence.imageId) }  // notify database changed
                    .flatMapCompletable { countUserImages().ignoreElement() }  // actualize user images count
                    .toSingleDefault(localImage)
            }

    private fun deleteUserImageRemote(essence: ImageDeleteEssence, retryCount: Int): Completable =
        local.userImage(id = essence.imageId)
            .onErrorResumeNext { error: Throwable ->
                Timber.e(error, "Image was not found in local cache, try backup instead")
                when (error) {
                    is NoSuchElementException -> imagesBackup.userImage(id = essence.imageId)
                    else -> Single.error(error)
                }
            }
            .flatMapCompletable { deleteUserImageRemoteImpl(localImage = it, essence = essence, retryCount = retryCount) }

    private fun deleteUserImageRemoteImpl(localImage: UserImage, essence: ImageDeleteEssence, retryCount: Int): Completable {
        fun addPendingDeleteImageRequest() {
            Completable.fromCallable { imageRequestLocal.addRequest(ImageRequest.from(essence)) }
                       .subscribeOn(Schedulers.io())
                       .subscribe({}, Timber::e)
        }

        return if (localImage.originId.isNullOrBlank()) {
            Report.w("Deleted local image that has no remote pair")
            Completable.complete()
        } else {
            spm.accessSingle { cloud.deleteUserImage(essence.copyWith(imageId = localImage.originId)) }
                .handleError(count = retryCount, tag = "deleteUserImage", traceTag = "image/delete_photo")
                .doOnDispose {
                    DebugLogUtil.i("Cancelled image delete")
                    addPendingDeleteImageRequest()
                    // TODO: not enough time to perform db insert, process terminates
                }
                .doOnError {
                    DebugLogUtil.w("Failed to delete image")
                    addPendingDeleteImageRequest()
                }
                .ignoreElement()
        }
    }

    /**
     * Perform deleting image remotely and then, if succeeded, locally. If failed, show error.
     * Perform less retries in order to fallback earlier in case of error.
     */
    private fun deleteUserImageSync(essence: ImageDeleteEssence, retryCount: Int): Completable =
        local.userImage(id = essence.imageId)
            .flatMap { localImage ->
                if (localImage.originId.isNullOrBlank()) {
                    Report.w("Deleted local image that has no remote pair")
                    Single.just(0L)
                } else {
                    spm.accessSingle { cloud.deleteUserImage(essence.copyWith(imageId = localImage.originId)) }
                        .handleError(count = minOf(3, retryCount) /* less retries */, tag = "deleteUserImage", traceTag = "image/delete_photo")
                }
            }
            .flatMap {
                Single.fromCallable { local.deleteUserImage(id = essence.imageId) }
                      .doOnSuccess { imageDeleted.onNext(essence.imageId) }  // notify database changed
            }
            .flatMap { countUserImages() }
            .ignoreElement()  // convert to Completable

    // ------------------------------------------
    override fun deleteLocalUserImages(): Completable =
        Completable.fromCallable { local.deleteAllUserImages() }
                   .andThen(Completable.fromCallable { imagesBackup.deleteAllUserImages() })

    override fun deleteLocalUserImageRequests(): Completable =
        Completable.fromCallable { imageRequestLocal.deleteAllRequests() }

    // ------------------------------------------------------------------------
    private fun prepareImageUploadUrlEssence(essence: IImageUploadUrlEssence, accessToken: AccessToken): ImageUploadUrlEssence =
        when (essence) {
            is ImageUploadUrlEssence -> essence  // for ImageUploadUrlEssence with access token supplied
            is ImageUploadUrlEssenceUnauthorized -> ImageUploadUrlEssence.from(essence, accessToken.accessToken)
            else -> throw IllegalArgumentException("Unsupported implementation of IImageUploadUrlEssence for createImage()")
        }

    override fun createImage(essence: IImageUploadUrlEssence, imageFilePath: String): Single<Image> =
        createImage(essence, imageFilePath, retryCount = BuildConfig.DEFAULT_RETRY_COUNT)

    /**
     * Performs crating image locally and then fails to create image on remote.
     */
    @DebugOnly
    override fun createImageFail(essence: IImageUploadUrlEssence, imageFilePath: String): Completable =
        spm.accessCompletable { accessToken ->
            createImageLocal(essence, imageFilePath)
                .flatMapCompletable { Completable.error(SimulatedException()) }
                .doOnError {
                    val xessence = prepareImageUploadUrlEssence(essence, accessToken)
                    Completable.fromCallable { imageRequestLocal.addRequest(ImageRequest.from(xessence, imageFilePath)) }
                               .subscribeOn(Schedulers.io())
                               .subscribe({}, Timber::e)
                }
        }

    private fun createImage(essence: IImageUploadUrlEssence, imageFilePath: String, retryCount: Int): Single<Image> =
        createImageLocal(essence, imageFilePath)
            .flatMap { (essence, localImage) ->
                createImageRemoteImpl(localImage = localImage, essence = essence,
                                      imageFilePath = imageFilePath, retryCount = retryCount)
            }

    private fun createImageLocal(essence: IImageUploadUrlEssence, imageFilePath: String): Single<Pair<ImageUploadUrlEssence, UserImage>> =
        spm.accessSingle { accessToken ->
            val imageFile = File(imageFilePath)
            val uriLocal = imageFile.uriString()
            val xessence = prepareImageUploadUrlEssence(essence, accessToken)
            val localImage = UserImage(id = xessence.clientImageId, uri = uriLocal, uriLocal = uriLocal)

            Single.fromCallable { local.addUserImage(localImage) }
                  .doOnSuccess { imageCreated.onNext(xessence.clientImageId) }  // notify database changed
                  .flatMapCompletable { countUserImages().ignoreElement() }  // actualize user images count
                  .toSingleDefault(xessence to localImage)
        }

    private fun createImageRemote(essence: ImageUploadUrlEssence, imageFilePath: String, retryCount: Int): Single<Image> =
        local.userImage(id = essence.clientImageId)
             .flatMap { createImageRemoteImpl(localImage = it, essence = essence,
                                              imageFilePath = imageFilePath, retryCount = retryCount) }

    private fun createImageRemoteImpl(localImage: UserImage, essence: ImageUploadUrlEssence,
                                      imageFilePath: String, retryCount: Int): Single<Image> {
        fun addPendingCreateImageRequest(imageFilePath: String) {
            Completable.fromCallable { imageRequestLocal.addRequest(ImageRequest.from(essence, imageFilePath)) }
                       .subscribeOn(Schedulers.io())
                       .subscribe({}, Timber::e)
        }

        return cloud.getImageUploadUrl(essence)
            .handleError(count = retryCount, tag = "createImage", traceTag = "image/get_presigned")
            .flatMap { image ->
                if (image.imageUri.isNullOrBlank()) {
                    val e = NullPointerException("Upload uri is null: $image")
                    Report.capture(e)
                    Single.error(e)
                } else {
                    /**
                     * Update [UserImageDbo.originId] and [UserImageDbo.uri] with remote-generated id and uri
                     * for image in local cache, keeping [UserImageDbo.id] and [UserImageDbo.uriLocal] unchanged.
                     */
                    val updatedLocalImage = localImage.copyWith(originId = image.originImageId, uri = image.imageUri)
                    // local image now has proper originId and remote url
                    Completable.fromCallable { local.updateUserImage(updatedLocalImage) }
                               .toSingleDefault(image)
                }
            }
            .flatMap {
                val imageFile = File(imageFilePath)
                cloud.uploadImage(url = it.imageUri!!, image = imageFile)
                     .handleError(count = retryCount, tag = "uploadImage", traceTag = "image/upload")
                     .andThen(Single.just(it))
            }
            .doOnDispose {
                DebugLogUtil.i("Cancelled image create and upload")
                addPendingCreateImageRequest(imageFilePath)
                // TODO: not enough time to perform db insert, process terminates
            }
            .doOnError {
                DebugLogUtil.w("Failed to create and upload image")
                addPendingCreateImageRequest(imageFilePath)
            }
            .map { it.map() }
    }

    // ------------------------------------------------------------------------
    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image> =
        spm.accessSingle { cloud.getImageUploadUrl(essence).map { it.map() } }

    override fun uploadImage(url: String, image: File): Completable = cloud.uploadImage(url = url, image = image)
}
