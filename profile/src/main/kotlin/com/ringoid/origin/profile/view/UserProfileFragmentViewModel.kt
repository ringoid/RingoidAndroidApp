package com.ringoid.origin.profile.view

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.image.*
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssenceUnauthorized
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.utility.extension
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

class UserProfileFragmentViewModel @Inject constructor(
    private val createUserImageUseCase: CreateUserImageUseCase,
    private val getUserImageByIdUseCase: GetUserImageByIdUseCase,
    private val deleteUserImageUseCase: DeleteUserImageUseCase,
    private val getUserImagesUseCase: GetUserImagesUseCase,
    private val getUserImagesAsyncUseCase: GetUserImagesAsyncUseCase, app: Application) : BaseViewModel(app) {

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
    fun onEventRefreshOnLmm(event: BusEvent.RefreshOnLmm) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Lmm screen leads Profile screen to refresh as well
        getUserImages()
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
                viewState.value = if (it.isEmpty()) ViewState.CLEAR(ViewState.CLEAR.MODE_EMPTY_DATA)
                                  else ViewState.IDLE
            }
            .autoDisposable(this)
            .subscribe({ images.value = it.apply { sortBy { it.sortPosition } } }, Timber::e)
    }

    fun deleteImage(id: String) {
        spm.accessToken()?.let {
            val essence = ImageDeleteEssence(accessToken = it.accessToken, imageId = id)

            deleteUserImageUseCase.source(params = Params().put(essence))
                .doOnSubscribe { viewState.value = ViewState.LOADING }
                .doOnComplete { viewState.value = ViewState.IDLE }
                .doOnError { viewState.value = ViewState.ERROR(it) }
                .autoDisposable(this)
                .subscribe({ Timber.d("Successfully deleted image: $it") }, Timber::e)
        }
    }

    fun uploadImage(uri: Uri) {
        val essence = ImageUploadUrlEssenceUnauthorized(extension = uri.extension())

        createUserImageUseCase.source(params = Params().put(essence).put("uri", uri))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ Timber.d("Successfully uploaded image: $it") }, Timber::e)
    }

    // ------------------------------------------
    fun onDeleteImage(empty: Boolean) {
        if (empty) {
            Bus.post(event = BusEvent.NoImagesOnProfile)
        }
    }

    fun onStartRefresh() {
        actionObjectPool.trigger()
    }

    fun onRefresh() {
        actionObjectPool.trigger()
        getUserImages()
        Bus.post(event = BusEvent.RefreshOnProfile)
    }

    fun onReferralClick() {
        if (!spm.hasReferralCode()) {
            // TODO: show dialog
        }
    }
}
