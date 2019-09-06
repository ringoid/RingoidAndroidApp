package com.ringoid.origin.error

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ringoid.origin.R
import com.ringoid.origin.navigation.blockingErrorScreen
import com.ringoid.origin.navigation.logout
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.report.exception.*
import com.ringoid.utility.delay

private fun showConnectionTimeoutErrorDialog(activity: FragmentActivity?, onRefresh: (() -> Unit)? = null) {
    Dialogs.showTextDialog(activity,
        descriptionResId = R.string.error_connection,
        positiveBtnLabelResId = R.string.button_retry,
        negativeBtnLabelResId = R.string.button_cancel,
        positiveListener = { dialog, _ -> onRefresh?.invoke(); dialog.dismiss() })
}

fun Throwable.handleOnView(activity: FragmentActivity, onErrorState: () -> Unit = {}, onRefresh: (() -> Unit)? = null) {
    fun errorState(e: Throwable) {
        Dialogs.errorDialog(activity, e)
        onErrorState.invoke()  // handle error state on Screen  in screen-specific way
    }

    fun noConnectionState(activity: FragmentActivity) {
        noConnection(activity)
        delay { onErrorState.invoke() }  // handle error state on Screen in a screen-specific way
    }

    when (this) {
        is OldAppVersionApiException -> blockingErrorScreen(activity, path = "/old_version")
        is InvalidAccessTokenApiException -> logout(activity)
        is NetworkUnexpected -> {
            when (this) {
                is ErrorConnectionTimedOut -> {
                    if (onRefresh != null) {
                        showConnectionTimeoutErrorDialog(activity, onRefresh)
                    } else {
                        noConnectionState(activity)
                    }
                }
                is ErrorConnectionInsecure,
                is ErrorNoConnection -> noConnectionState(activity)
                else -> errorState(this)
            }
        }
        else -> errorState(this)  // default error handling
    }
}

fun Throwable.handleOnView(fragment: Fragment, onErrorState: () -> Unit = {}, onRefresh: (() -> Unit)? = null) {
    fun errorState(e: Throwable) {
        Dialogs.errorDialog(fragment, e)
        onErrorState.invoke()  // handle error state on Screen  in screen-specific way
    }

    fun noConnectionState(fragment: Fragment) {
        noConnection(fragment)
        delay { onErrorState.invoke() }  // handle error state on Screen in a screen-specific way
    }

    when (this) {
        is OldAppVersionApiException -> blockingErrorScreen(fragment, path = "/old_version")
        is InvalidAccessTokenApiException -> logout(fragment)
        is NetworkUnexpected -> {
            when (this) {
                is ErrorConnectionTimedOut -> {
                    if (onRefresh != null) {
                        showConnectionTimeoutErrorDialog(fragment.activity, onRefresh)
                    } else {
                        noConnectionState(fragment)
                    }
                }
                is ErrorConnectionInsecure,
                is ErrorNoConnection -> noConnectionState(fragment)
                else -> errorState(this)
            }
        }
        else -> errorState(this)  // default error handling
    }
}
