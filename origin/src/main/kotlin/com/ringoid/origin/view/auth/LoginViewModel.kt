package com.ringoid.origin.view.auth

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.misc.Gender
import com.ringoid.origin.RingoidApplication
import com.ringoid.utility.isAdultAge
import com.ringoid.widget.WidgetState
import javax.inject.Inject

class LoginViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    val calendar by lazy { getApplication<RingoidApplication>().calendar }

    val loginButtonEnableState by lazy { MutableLiveData<Boolean>() }
    val yearOfBirthEntryState by lazy { MutableLiveData<WidgetState>() }

    var gender: Gender? = null
        set (value) {
            field = value
            enableLoginButton()
        }

    // --------------------------------------------------------------------------------------------
    fun login() {
        // TODO: get year and gender
    }

    fun onYearOfBirthChange(text: String) {
        yearOfBirthEntryState.value =
                text.toIntOrNull()
                    ?.takeIf { isAdultAge(it, calendar) }
                    ?.let { WidgetState.ACTIVE }
                    ?: WidgetState.ERROR

        enableLoginButton()
    }

    // --------------------------------------------------------------------------------------------
    private fun enableLoginButton(){
        val isValidYearOfBirth = yearOfBirthEntryState.value == WidgetState.ACTIVE
        loginButtonEnableState.value = isValidYearOfBirth && gender != null
    }
}
