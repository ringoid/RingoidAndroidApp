package com.ringoid.origin.profile.dialog.delete

import com.ringoid.utility.DebugOnly
import com.ringoid.utility.ICommunicator

@Deprecated("Replaced with context menu")
interface IDeleteUserProfileImageActivity : ICommunicator {

    fun onClose()
    fun onImageDelete()

    @DebugOnly
    fun onImageDeleteDebugAndClose()
}
