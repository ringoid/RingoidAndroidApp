package com.ringoid.origin.profile.dialog.context_menu

import com.ringoid.utility.DebugOnly
import com.ringoid.utility.ICommunicator

interface IUserProfileContextMenuActivity : ICommunicator {

    fun onClose()
    fun onAddImage()
    fun onDeleteImage()
    @DebugOnly fun onDeleteImageDebug()
    fun onEditProfile()
    fun onEditStatus()
    fun openSocialInstagram()
    fun openSocialTiktok()
}
