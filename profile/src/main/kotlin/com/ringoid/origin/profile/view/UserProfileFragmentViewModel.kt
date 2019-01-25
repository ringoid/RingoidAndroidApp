package com.ringoid.origin.profile.view

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.image.CreateUserImageUseCase
import com.ringoid.domain.interactor.image.DeleteUserImageUseCase
import com.ringoid.domain.interactor.image.GetUserImageByIdUseCase
import com.ringoid.domain.interactor.image.GetUserImagesUseCase
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssenceUnauthorized
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.ScreenHelper
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.utility.extension
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class UserProfileFragmentViewModel @Inject constructor(
    private val createUserImageUseCase: CreateUserImageUseCase,
    private val getUserImageByIdUseCase: GetUserImageByIdUseCase,
    private val deleteUserImageUseCase: DeleteUserImageUseCase,
    private val getUserImagesUseCase: GetUserImagesUseCase,
    app: Application) : BaseViewModel(app) {

    val imageCreated by lazy { MutableLiveData<UserImage>() }
    val imageDeleted by lazy { MutableLiveData<String>() }
    val imageIdChanged by lazy { MutableLiveData<Pair<String, String>>() }
    val images by lazy { MutableLiveData<List<UserImage>>() }

    init {
        createUserImageUseCase.repository.imageCreate
            .autoDisposable(this)
            .subscribe {
                getUserImageByIdUseCase.source(Params().put("id", it))
                    .autoDisposable(this)
                    .subscribe({ imageCreated.value = it }, Timber::e)
            }

        createUserImageUseCase.repository.imageDelete
            .observeOn(AndroidSchedulers.mainThread())  // touch LiveData on main thread only
            .autoDisposable(this)
            .subscribe({ imageDeleted.value = it }, Timber::e)

        createUserImageUseCase.repository.imageIdChange
            .observeOn(AndroidSchedulers.mainThread())  // touch LiveData on main thread only
            .autoDisposable(this)
            .subscribe({ imageIdChanged.value = it }, Timber::e)
    }

    fun getUserImages() {
        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))

        getUserImagesUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ images.value = it }, Timber::e)
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
    fun onAddImage() {
        navigation.value = ExternalNavigator::openGalleryToGetImageFragment
    }

    fun onRefresh() {
        getUserImages()
        Bus.post(event = BusEvent.RefreshOnProfile)
    }
}
