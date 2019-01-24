package com.ringoid.origin.auth.view

import android.app.Application
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.CreateUserProfileUseCase
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.origin.BaseRingoidApplication
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.utility.isAdultAge
import com.ringoid.widget.WidgetState
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val createUserProfileUseCase: CreateUserProfileUseCase, app: Application)
    : BaseViewModel(app) {

    private val calendar: Calendar by lazy { getApplication<BaseRingoidApplication>().calendar }

    val loginButtonEnableState by lazy { MutableLiveData<Boolean>() }
    val yearOfBirthEntryState by lazy { MutableLiveData<WidgetState>() }

    private var gender: Gender? = null
        set (value) {
            field = value
            enableLoginButton()
        }
    private var yearOfBirth: Int = 0

    // --------------------------------------------------------------------------------------------
    fun login() {
        val essence = AuthCreateProfileEssence(
            yearOfBirth = yearOfBirth,
            sex = gender?.string ?: Gender.MALE.string /* safe null-check */,
            device = String.format("%s, %d", Build.VERSION.RELEASE, Build.VERSION.SDK_INT),
            osVersion = String.format("%s, %s, %s", Build.MODEL, Build.MANUFACTURER, Build.PRODUCT))

        createUserProfileUseCase.source(params = Params().put(essence))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.CLOSE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                Timber.d("Successfully logged in, current user: $it")
                navigation.value = ExternalNavigator::openGalleryToGetImage
            }, Timber::e)
    }

    fun onGenderSelect(gender: Gender) {
        this.gender = gender
    }

    fun onYearOfBirthChange(text: String) {
        yearOfBirthEntryState.value =
                text.takeIf { it.isEmpty() }
                    ?.let { WidgetState.NORMAL }
                    ?: run {
                        text.toIntOrNull()
                            ?.takeIf { isAdultAge(it, calendar) }
                            ?.let { yearOfBirth = it; WidgetState.ACTIVE }
                            ?: WidgetState.ERROR
                    }

        enableLoginButton()
    }

    // --------------------------------------------------------------------------------------------
    private fun enableLoginButton(){
        val isValidYearOfBirth = yearOfBirthEntryState.value == WidgetState.ACTIVE
        loginButtonEnableState.value = isValidYearOfBirth && gender != null
    }
}
