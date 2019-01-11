package com.ringoid.origin.profile.view.profile

import android.app.Application
import android.net.Uri
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.image.CreateImageUseCase
import com.ringoid.domain.interactor.image.GetUserImagesUseCase
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssenceUnauthorized
import com.ringoid.utility.extension
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class ProfileFragmentViewModel @Inject constructor(
    private val createImageUseCase: CreateImageUseCase, private val getUserImagesUseCase: GetUserImagesUseCase,
    app: Application) : BaseViewModel(app) {

    fun getUserImages() {
        //
    }

    fun uploadImage(uri: Uri) {
        val essence = ImageUploadUrlEssenceUnauthorized(extension = uri.extension())

        createImageUseCase.source(params = Params().put(essence).put("uri", uri))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.DONE(IMAGE_CREATED) }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ Timber.d("Successfully uploaded image: $it") }, Timber::e)
    }
}
