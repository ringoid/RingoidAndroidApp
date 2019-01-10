package com.ringoid.origin.profile.view.image

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.image.DeleteUserImageUseCase
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.origin.profile.view.profile.IMAGE_DELETED
import com.ringoid.origin.view.image.ImagePageViewModel
import com.uber.autodispose.AutoDispose
import timber.log.Timber
import javax.inject.Inject

class ProfileImagePageViewModel @Inject constructor(
    private val deleteUserImageUseCase: DeleteUserImageUseCase, app: Application)
    : ImagePageViewModel(app) {

    fun deleteImage(id: String) {
        spm.accessToken()?.let {
            val essence = ImageDeleteEssence(accessToken = it.accessToken, imageId = id)

            deleteUserImageUseCase.source(params = Params().put(essence))
                .doOnSubscribe { viewState.value = ViewState.LOADING }
                .doOnComplete { viewState.value = ViewState.DONE(IMAGE_DELETED) }
                .doOnError { viewState.value = ViewState.ERROR(it) }
                .`as`(AutoDispose.autoDisposable(this))
                .subscribe({ Timber.d("Successfully deleted image: $it") }, Timber::e)
        }
    }
}
