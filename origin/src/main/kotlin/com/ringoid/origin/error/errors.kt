package com.ringoid.origin.error

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ringoid.domain.exception.ApiException
import com.ringoid.domain.exception.NetworkUnexpected
import com.ringoid.origin.navigation.blockingErrorScreen
import com.ringoid.origin.navigation.logout
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.Dialogs

fun Throwable.handleOnView(activity: FragmentActivity, onErrorState: () -> Unit = {}) {
    fun errorState(e: Throwable) {
        Dialogs.errorDialog(activity, e)
        onErrorState()  // handle error state on Screen  in screen-specific way
    }

    when (this) {
        is ApiException -> {
            when (code) {
                ApiException.OLD_APP_VERSION -> blockingErrorScreen(activity, path = "/old_version")
                ApiException.INVALID_ACCESS_TOKEN -> logout(activity)
                else -> errorState(this)
            }
        }
        is NetworkUnexpected -> {
            noConnection(activity)
            activity.runOnUiThread {onErrorState() }  // handle error state on Screen  in screen-specific way
        }
        else -> errorState(this)  // default error handling
    }
}

fun Throwable.handleOnView(fragment: Fragment, onErrorState: () -> Unit = {}) {
    fun errorState(e: Throwable) {
        Dialogs.errorDialog(fragment, e)
        onErrorState()  // handle error state on Screen  in screen-specific way
    }

    when (this) {
        is ApiException -> {
            when (code) {
                ApiException.OLD_APP_VERSION -> blockingErrorScreen(fragment, path = "/old_version")
                ApiException.INVALID_ACCESS_TOKEN -> logout(fragment)
                else -> errorState(this)
            }
        }
        is NetworkUnexpected -> {
            noConnection(fragment)
            fragment.activity?.runOnUiThread { onErrorState() }  // handle error state on Screen  in screen-specific way
        }
        else -> errorState(this)  // default error handling
    }
}
