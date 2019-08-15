package com.ringoid.origin.usersettings.view.profile

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.interactor.user.UpdateUserProfileSettingsUseCase
import com.ringoid.origin.model.*
import com.ringoid.origin.usersettings.view.base.BaseSettingsViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsProfileViewModel @Inject constructor(
    private val updateUserProfileSettingsUseCase: UpdateUserProfileSettingsUseCase,
    postToSlackUseCase: PostToSlackUseCase, app: Application)
    : BaseSettingsViewModel(postToSlackUseCase, app) {

    val profile by lazy { MutableLiveData<UserProfileProperties>() }

    private lateinit var properties: UserProfileProperties

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        properties = UserProfileProperties.from(spm.getUserProfileProperties())
        profile.value = properties
    }

    /* Properties */
    // --------------------------------------------------------------------------------------------
    fun onPropertyChanged_children(children: ChildrenProfileProperty) {
        if (properties.children == children) {
            return
        }
        properties.children = children
        updateProfileProperties(propertyName = children.name)
    }

    fun onPropertyChanged_education(education: EducationProfileProperty) {
        if (properties.education == education) {
            return
        }
        properties.education = education
        updateProfileProperties(propertyName = education.name)
    }

    fun onPropertyChanged_hairColor(hairColor: HairColorProfileProperty) {
        if (properties.hairColor == hairColor) {
            return
        }
        properties.hairColor = hairColor
        updateProfileProperties(propertyName = hairColor.name)
    }

    fun onPropertyChanged_income(income: IncomeProfileProperty) {
        if (properties.income == income) {
            return
        }
        properties.income = income
        updateProfileProperties(propertyName = income.name)
    }

    fun onPropertyChanged_property(property: PropertyProfileProperty) {
        if (properties.property == property) {
            return
        }
        properties.property = property
        updateProfileProperties(propertyName = property.name)
    }

    fun onPropertyChanged_transport(transport: TransportProfileProperty) {
        if (properties.transport == transport) {
            return
        }
        properties.transport = transport
        updateProfileProperties(propertyName = transport.name)
    }

    /* Custom Properties */
    // --------------------------------------------------------------------------------------------
    fun onCustomPropertyChanged_about(text: String) {
        if (properties.about() == text) {
            return
        }
        properties.about(text)
        updateProfileProperties(propertyName = "about")
    }

    fun onCustomPropertyChanged_company(text: String) {
        if (properties.company() == text) {
            return
        }
        properties.company(text)
        updateProfileProperties(propertyName = "company")
    }

    fun onCustomPropertyChanged_jobTitle(text: String) {
        if (properties.jobTitle() == text) {
            return
        }
        properties.jobTitle(text)
        updateProfileProperties(propertyName = "jobTitle")
    }

    fun onCustomPropertyChanged_height(height: Int) {
        if (properties.height == height || height in 1..91) {
            return
        }
        properties.height = height
        updateProfileProperties(propertyName = "height")
    }

    fun onCustomPropertyChanged_name(text: String) {
        if (properties.name() == text) {
            return
        }
        properties.name(text)
        updateProfileProperties(propertyName = "name")
    }

    fun onCustomPropertyChanged_socialInstagram(text: String) {
        if (properties.instagram() == text) {
            return
        }
        properties.instagram(text)
        updateProfileProperties(propertyName = "instagram")
    }

    fun onCustomPropertyChanged_socialTikTok(text: String) {
        if (properties.tiktok() == text) {
            return
        }
        properties.tiktok(text)
        updateProfileProperties(propertyName = "tiktok")
    }

    fun onCustomPropertyChanged_university(text: String) {
        if (properties.university() == text) {
            return
        }
        properties.university(text)
        updateProfileProperties(propertyName = "education")
    }

    fun onCustomPropertyChanged_whereLive(text: String) {
        if (properties.whereLive() == text) {
            return
        }
        properties.whereLive(text)
        updateProfileProperties(propertyName = "whereLive")
    }

    // --------------------------------------------------------------------------------------------
    private fun updateProfileProperties(propertyName: String) {
        updateUserProfileSettingsUseCase.source(Params().put(properties.map()))
            .doOnSubscribe {
                viewState.value = ViewState.LOADING
                spm.setUserProfileProperties(propertiesRaw = properties.map())
            }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .doOnComplete { viewState.value = ViewState.IDLE }
            .doFinally { analyticsManager.fireOnce(Analytics.AHA_FIRST_FIELD_SET, "fieldName" to propertyName) }
            .autoDisposable(this)
            .subscribe({ Timber.d("Successfully updated user profile properties") }, Timber::e)
    }
}
