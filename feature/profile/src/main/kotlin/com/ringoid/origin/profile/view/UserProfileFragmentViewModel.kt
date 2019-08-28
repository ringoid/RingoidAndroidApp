package com.ringoid.origin.profile.view

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.interactor.base.CompletableUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.image.*
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.essence.image.ImageDeleteEssenceUnauthorized
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssenceUnauthorized
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.model.UserProfileProperties
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import com.ringoid.utility.extension
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserProfileFragmentViewModel @Inject constructor(
    private val createUserImageUseCase: CreateUserImageUseCase,
    private val getUserImageByIdUseCase: GetUserImageByIdUseCase,
    private val deleteUserImageUseCase: DeleteUserImageUseCase,
    private val getUserImagesUseCase: GetUserImagesUseCase,
    private val getUserImagesAsyncUseCase: GetUserImagesAsyncUseCase,
    @DebugOnly private val failedCreateUserImageUseCase: FailedCreateUserImageUseCase,
    @DebugOnly private val failedDeleteUserImageUseCase: FailedDeleteUserImageUseCase, app: Application)
    : BasePermissionViewModel(app) {

    private val imageBlocked by lazy { MutableLiveData<String>() }
    private val imageCreated by lazy { MutableLiveData<UserImage>() }
    private val imageDeleted by lazy { MutableLiveData<String>() }
    private val images by lazy { MutableLiveData<List<UserImage>>() }
    private val profile by lazy { MutableLiveData<UserProfileProperties>() }
    internal fun imageBlocked(): LiveData<String> = imageBlocked
    internal fun imageCreated(): LiveData<UserImage> = imageCreated
    internal fun imageDeleted(): LiveData<String> = imageDeleted
    internal fun images(): LiveData<List<UserImage>> = images
    internal fun profile(): LiveData<UserProfileProperties> = profile

    init {
        createUserImageUseCase.repository.imageBlocked  // debounce to handle image blocked just once
            .debounce(500L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())  // touch LiveData on main thread only
            .autoDisposable(this)
            .subscribe({ imageBlocked.value = it }, DebugLogUtil::e)

        createUserImageUseCase.repository.imageCreated
            .autoDisposable(this)
            .subscribe {
                getUserImageByIdUseCase.source(Params().put("id", it))
                    .autoDisposable(this)
                    .subscribe({ imageCreated.value = it }, DebugLogUtil::e)
            }

        createUserImageUseCase.repository.imageDeleted
            .observeOn(AndroidSchedulers.mainThread())  // touch LiveData on main thread only
            .autoDisposable(this)
            .subscribe({ imageDeleted.value = it }, DebugLogUtil::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onStart() {
        super.onStart()
        profile.value = UserProfileProperties.from(spm.getUserProfileProperties())
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReOpenApp(event: BusEvent.ReOpenAppOnPush) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        onRefresh()  // app reopen leads Profile screen to refresh
    }

    // --------------------------------------------------------------------------------------------
    private fun getUserImages() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

        getUserImagesAsyncUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .flatMap {
                Observable.fromIterable(it)
                    .filter { !it.isBlocked }
                    .toList()
                    .toObservable()
            }
            .doOnNext {
                viewState.value = if (it.isEmpty()) ViewState.CLEAR(ViewState.CLEAR.MODE_EMPTY_DATA)  // no images in Profile
                                  else ViewState.IDLE
            }
            .autoDisposable(this)
            .subscribe({ images.value = it.apply { sortBy { it.sortPosition } } }, DebugLogUtil::e)
    }

    fun deleteImage(id: String) {
        deleteImageImpl(id = id, useCase = deleteUserImageUseCase)
    }

    @DebugOnly
    fun deleteImageDebug(id: String) {
        deleteImageImpl(id = id, useCase = failedDeleteUserImageUseCase)
    }

    private fun deleteImageImpl(id: String, useCase: CompletableUseCase) {
        useCase.source(params = Params().put(ImageDeleteEssenceUnauthorized(imageId = id)))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                Timber.d("Successfully deleted image: $id")
                analyticsManager.fire(Analytics.IMAGE_USER_DELETE_PHOTO)
            }, DebugLogUtil::e)
    }

    fun uploadImage(uri: Uri) {
        val essence = ImageUploadUrlEssenceUnauthorized(extension = uri.extension())

        createUserImageUseCase.source(params = Params().put(essence).put("uri", uri))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                Timber.d("Successfully uploaded image: $it")
                analyticsManager.fire(Analytics.IMAGE_USER_UPLOAD_PHOTO)
                analyticsManager.fireOnce(Analytics.AHA_PHOTO_ADDED_MANUALLY)
            }, DebugLogUtil::e)
    }

    @DebugOnly
    fun uploadImageDebug(uri: Uri) {
        val essence = ImageUploadUrlEssenceUnauthorized(extension = uri.extension())

        failedCreateUserImageUseCase.source(params = Params().put(essence).put("uri", uri))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({}, DebugLogUtil::e)
    }

    // ------------------------------------------
    override fun onLocationReceived(handleCode: Int) {
        super.onLocationReceived(handleCode)
        when (handleCode) {
            HC_ADD_IMAGE -> viewState.value = ViewState.DONE(REQUEST_TO_ADD_IMAGE)
            HC_REFRESH -> onRefresh()
        }
    }

    override fun onLocationPermissionDeniedAction(handleCode: Int) {
        super.onLocationPermissionDeniedAction(handleCode)
        when (handleCode) {
            HC_REFRESH -> onRefresh()  // TODO: use cached
        }
    }

    fun onStartRefresh() {
        analyticsManager.fire(Analytics.PULL_TO_REFRESH, "sourceFeed" to DomainUtil.SOURCE_FEED_PROFILE)
    }

    fun onRefresh() {
        getUserImages()
    }
}
