package com.ringoid.origin.usersettings.view.profile

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.UpdateUserProfileSettingsUseCase
import com.ringoid.origin.model.*
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsProfileViewModel @Inject constructor(
    private val updateUserProfileSettingsUseCase: UpdateUserProfileSettingsUseCase,
    app: Application) : BaseViewModel(app) {

    val profile by lazy { MutableLiveData<UserProfileProperties>() }

    private lateinit var properties: UserProfileProperties

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        properties = UserProfileProperties.from(spm.getUserProfileProperties())
        profile.value = properties
    }

    // --------------------------------------------------------------------------------------------
    fun onPropertyChanged_children(children: ChildrenProfileProperty) {
        if (properties.children == children) {
            return
        }
        properties.children = children
        updateProfileProperties()
    }

    fun onPropertyChanged_education(education: EducationProfileProperty) {
        if (properties.education == education) {
            return
        }
        properties.education = education
        updateProfileProperties()
    }

    fun onPropertyChanged_hairColor(hairColor: HairColorProfileProperty) {
        if (properties.hairColor == hairColor) {
            return
        }
        properties.hairColor = hairColor
        updateProfileProperties()
    }

    fun onPropertyChanged_height(height: Int) {
        if (properties.height == height || height in 1..91) {
            return
        }
        properties.height = height
        updateProfileProperties()
    }

    fun onPropertyChanged_income(income: IncomeProfileProperty) {
        if (properties.income == income) {
            return
        }
        properties.income = income
        updateProfileProperties()
    }

    fun onPropertyChanged_property(property: PropertyProfileProperty) {
        if (properties.property == property) {
            return
        }
        properties.property = property
        updateProfileProperties()
    }

    fun onPropertyChanged_transport(transport: TransportProfileProperty) {
        if (properties.transport == transport) {
            return
        }
        properties.transport = transport
        updateProfileProperties()
    }

    // ------------------------------------------
    private fun updateProfileProperties() {
        updateUserProfileSettingsUseCase.source(Params().put(properties.map()))
            .doOnSubscribe {
                viewState.value = ViewState.LOADING
                spm.setUserProfileProperties(propertiesRaw = properties.map())
            }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .doOnComplete { viewState.value = ViewState.IDLE }
            .autoDisposable(this)
            .subscribe({ Timber.d("Successfully updated user profile properties") }, Timber::e)
    }
}
