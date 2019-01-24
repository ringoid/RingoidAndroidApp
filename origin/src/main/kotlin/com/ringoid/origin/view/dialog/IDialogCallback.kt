package com.ringoid.origin.view.dialog

import com.ringoid.utility.ICommunicator

interface IDialogCallback : ICommunicator {

    fun onDialogDismiss(tag: String)
}
