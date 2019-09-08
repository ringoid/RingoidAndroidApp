package com.ringoid.origin.profile.dialog.delete

import com.ringoid.utility.DebugOnly
import com.ringoid.utility.ICommunicator

interface IDeleteUserProfileImageActivity : ICommunicator {

    fun onClose()
    fun onImageDelete()

    @DebugOnly
    fun onImageDeleteDebugAndClose()
}
