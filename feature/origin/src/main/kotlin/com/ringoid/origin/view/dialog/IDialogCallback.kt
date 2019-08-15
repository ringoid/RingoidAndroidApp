package com.ringoid.origin.view.dialog

import android.os.Parcelable
import com.ringoid.utility.ICommunicator

interface IDialogCallback : ICommunicator {

    fun onDialogDismiss(tag: String, payload: Parcelable? = null)
}
