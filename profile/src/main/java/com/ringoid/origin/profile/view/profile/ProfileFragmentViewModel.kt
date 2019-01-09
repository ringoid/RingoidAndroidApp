package com.ringoid.origin.profile.view.profile

import android.app.Application
import android.net.Uri
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.image.CreateImageUseCase
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import timber.log.Timber
import javax.inject.Inject

class ProfileFragmentViewModel @Inject constructor(
    private val createImageUseCase: CreateImageUseCase, app: Application)
    : BaseViewModel(app) {

    fun uploadImage(uri: Uri) {
        val essence = ImageUploadUrlEssence()

        val params = Params().put(essence)

        subs = createImageUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .subscribe({
                Timber.d("Successfully uploaded image: $it")
                // TODO: do smth on image uploaded
            }, Timber::e)
    }
}
