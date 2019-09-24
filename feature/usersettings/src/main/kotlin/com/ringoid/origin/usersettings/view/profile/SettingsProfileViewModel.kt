package com.ringoid.origin.usersettings.view.profile

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.interactor.user.UpdateUserProfileSettingsUseCase
import com.ringoid.domain.misc.UserProfileCustomPropertiesUnsavedInput
import com.ringoid.origin.model.*
import com.ringoid.origin.view.base.settings.BaseSettingsViewModel
import com.ringoid.origin.view.base.settings.SimpleBaseSettingsViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsProfileViewModel @Inject constructor(
    private val updateUserProfileSettingsUseCase: UpdateUserProfileSettingsUseCase,
    postToSlackUseCase: PostToSlackUseCase, app: Application)
    : SimpleBaseSettingsViewModel(postToSlackUseCase, app) {

    private val profile by lazy { MutableLiveData<UserProfileProperties>() }
    internal fun profile(): LiveData<UserProfileProperties> = profile

    private lateinit var properties: UserProfileProperties
    private lateinit var unsavedProperties: UserProfileCustomPropertiesUnsavedInput

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        properties = UserProfileProperties.from(spm.getUserProfileProperties())
        unsavedProperties = spm.getUserProfileCustomPropertiesUnsavedInput()
        profile.value = properties  // assign initial values to views
        spm.dropNeedShowStubStatus()  // drop flag
    }

    override fun onStop() {
        super.onStop()
        spm.setUserProfileCustomPropertiesUnsavedInput(unsavedProperties)
    }

    /* Properties */
    // --------------------------------------------------------------------------------------------
    fun onPropertyChanged_children(children: ChildrenProfileProperty) {
        if (properties.children == children) {
            return
        }
        properties.children = children
        updateProfileProperties(propertyNameForAnalytics = children.name)
    }

    fun onPropertyChanged_education(education: EducationProfileProperty) {
        if (properties.education == education) {
            return
        }
        properties.education = education
        updateProfileProperties(propertyNameForAnalytics = education.name)
    }

    fun onPropertyChanged_hairColor(hairColor: HairColorProfileProperty) {
        if (properties.hairColor == hairColor) {
            return
        }
        properties.hairColor = hairColor
        updateProfileProperties(propertyNameForAnalytics = hairColor.name)
    }

    fun onPropertyChanged_income(income: IncomeProfileProperty) {
        if (properties.income == income) {
            return
        }
        properties.income = income
        updateProfileProperties(propertyNameForAnalytics = income.name)
    }

    fun onPropertyChanged_property(property: PropertyProfileProperty) {
        if (properties.property == property) {
            return
        }
        properties.property = property
        updateProfileProperties(propertyNameForAnalytics = property.name)
    }

    fun onPropertyChanged_transport(transport: TransportProfileProperty) {
        if (properties.transport == transport) {
            return
        }
        properties.transport = transport
        updateProfileProperties(propertyNameForAnalytics = transport.name)
    }

    /* Custom Properties */
    // --------------------------------------------------------------------------------------------
    internal fun onCustomPropertyChanged_about(text: String) {
        if (properties.about() == text) {
            return
        }
        properties.about(text)
        updateProfileProperties(propertyNameForAnalytics = "about")
    }

    internal fun onCustomPropertyUnsavedInput_about(text: String) {
        unsavedProperties.about = text
    }

    internal fun getCustomPropertyUnsavedInput_about(): String = unsavedProperties.about

    internal fun onCustomPropertyChanged_company(text: String) {
        if (properties.company() == text) {
            return
        }
        properties.company(text)
        updateProfileProperties(propertyNameForAnalytics = "company")
    }

    internal fun onCustomPropertyChanged_jobTitle(text: String) {
        if (properties.jobTitle() == text) {
            return
        }
        properties.jobTitle(text)
        updateProfileProperties(propertyNameForAnalytics = "jobTitle")
    }

    internal fun onCustomPropertyChanged_height(height: Int) {
        if (properties.height == height || height in 1..91) {
            return
        }
        properties.height = height
        updateProfileProperties(propertyNameForAnalytics = "height")
    }

    internal fun onCustomPropertyChanged_name(text: String) {
        if (properties.name() == text) {
            return
        }
        properties.name(text)
        updateProfileProperties(propertyNameForAnalytics = "name")
    }

    internal fun onCustomPropertyChanged_status(text: String) {
        if (properties.status() == text) {
            return
        }
        properties.status(text)
        updateProfileProperties(propertyNameForAnalytics = "status")
    }

    internal fun onCustomPropertyChanged_socialInstagram(text: String) {
        if (properties.instagram() == text) {
            return
        }
        properties.instagram(text)
        updateProfileProperties(propertyNameForAnalytics = "instagram")
    }

    internal fun onCustomPropertyChanged_socialTikTok(text: String) {
        if (properties.tiktok() == text) {
            return
        }
        properties.tiktok(text)
        updateProfileProperties(propertyNameForAnalytics = "tiktok")
    }

    internal fun onCustomPropertyChanged_university(text: String) {
        if (properties.university() == text) {
            return
        }
        properties.university(text)
        updateProfileProperties(propertyNameForAnalytics = "education")
    }

    internal fun onCustomPropertyChanged_whereLive(text: String) {
        if (properties.whereLive() == text) {
            return
        }
        properties.whereLive(text)
        updateProfileProperties(propertyNameForAnalytics = "whereLive")
    }

    // --------------------------------------------------------------------------------------------
    private fun updateProfileProperties(propertyNameForAnalytics: String) {
        updateUserProfileSettingsUseCase.source(Params().put(properties.map()))
            .doOnSubscribe {
                viewState.value = ViewState.LOADING  // update user profile properties progress
                spm.setUserProfileProperties(propertiesRaw = properties.map())
            }
            .doOnComplete { viewState.value = ViewState.IDLE }  // update user profile properties success
            .doOnError { viewState.value = ViewState.ERROR(it) }  // update user profile properties failed
            .doFinally { analyticsManager.fireOnce(Analytics.AHA_FIRST_FIELD_SET, "fieldName" to propertyNameForAnalytics) }
            .autoDisposable(this)
            .subscribe({ Timber.d("Successfully updated user profile properties") }, DebugLogUtil::e)
    }

    internal fun retryOnError() {
        updateProfileProperties("")
    }
}
