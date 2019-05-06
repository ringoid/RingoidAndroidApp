package com.ringoid.origin.profile.view

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.WrongRequestParamsClientApiException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.image.*
import com.ringoid.domain.interactor.user.ApplyReferralCodeUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.essence.image.ImageDeleteEssenceUnauthorized
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssenceUnauthorized
import com.ringoid.domain.model.essence.user.ReferralCodeEssenceUnauthorized
import com.ringoid.domain.model.image.UserImage
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
    private val applyReferralCodeUseCase: ApplyReferralCodeUseCase,
    private val createUserImageUseCase: CreateUserImageUseCase,
    private val getUserImageByIdUseCase: GetUserImageByIdUseCase,
    private val deleteUserImageUseCase: DeleteUserImageUseCase,
    private val getUserImagesUseCase: GetUserImagesUseCase,
    private val getUserImagesAsyncUseCase: GetUserImagesAsyncUseCase, app: Application) : BasePermissionViewModel(app) {

    val imageBlocked by lazy { MutableLiveData<String>() }
    val imageCreated by lazy { MutableLiveData<UserImage>() }
    val imageDeleted by lazy { MutableLiveData<String>() }
    val images by lazy { MutableLiveData<List<UserImage>>() }

    init {
        createUserImageUseCase.repository.imageBlocked  // debounce to handle image blocked just once
            .debounce(500L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())  // touch LiveData on main thread only
            .autoDisposable(this)
            .subscribe({ imageBlocked.value = it }, Timber::e)

        createUserImageUseCase.repository.imageCreated
            .autoDisposable(this)
            .subscribe {
                getUserImageByIdUseCase.source(Params().put("id", it))
                    .autoDisposable(this)
                    .subscribe({ imageCreated.value = it }, Timber::e)
            }

        createUserImageUseCase.repository.imageDeleted
            .observeOn(AndroidSchedulers.mainThread())  // touch LiveData on main thread only
            .autoDisposable(this)
            .subscribe({ imageDeleted.value = it }, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnExplore(event: BusEvent.RefreshOnExplore) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Explore Feed screen leads Profile screen to refresh as well
        getUserImages()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnLmm(event: BusEvent.RefreshOnLmm) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Lmm screen leads Profile screen to refresh as well
        getUserImages()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReOpenApp(event: BusEvent.ReOpenApp) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        onRefresh()  // app reopen leads Profile screen to refresh cascade
    }

    // --------------------------------------------------------------------------------------------
    private fun getUserImages() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

        getUserImagesAsyncUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.PROGRESS }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .flatMap {
                Observable.fromIterable(it)
                    .filter { !it.isBlocked }
                    .toList()
                    .toObservable()
            }
            .doOnNext {
                viewState.value = if (it.isEmpty()) ViewState.CLEAR(ViewState.CLEAR.MODE_EMPTY_DATA)
                                  else ViewState.IDLE
            }
            .autoDisposable(this)
            .subscribe({ images.value = it.apply { sortBy { it.sortPosition } } }, Timber::e)
    }

    fun deleteImage(id: String) {
        deleteUserImageUseCase.source(params = Params().put(ImageDeleteEssenceUnauthorized(imageId = id)))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                Timber.d("Successfully deleted image: $id")
                analyticsManager.fire(Analytics.IMAGE_USER_DELETE_PHOTO)
            }, Timber::e)
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
            }, Timber::e)
    }

    // ------------------------------------------
    fun applyReferralCode(code: String?) {
        if (code.isNullOrBlank()) {
            return  // apply nothing
        }

        DebugLogUtil.d("Applying referral code: $code")
        applyReferralCodeUseCase.source(params = Params().put(ReferralCodeEssenceUnauthorized(referralId = code!!)))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnError {
                viewState.value = when (it) {
                    is WrongRequestParamsClientApiException -> ViewState.DONE(REFERRAL_CODE_DECLINED)
                    else -> ViewState.ERROR(it)
                }
            }
            .autoDisposable(this)
            .subscribe({
                Timber.d("Successfully applied referral code: $code")
                spm.setReferralCode(code)  // save accepted referral code
                viewState.value = ViewState.DONE(REFERRAL_CODE_ACCEPTED)
            }, Timber::e)
    }

    // ------------------------------------------
    fun onDeleteImage(empty: Boolean) {
        if (empty) {
            Bus.post(event = BusEvent.NoImagesOnProfile)
        }
    }

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
        actionObjectPool.trigger()
        getUserImages()
        Bus.post(event = BusEvent.RefreshOnProfile)
    }
}
