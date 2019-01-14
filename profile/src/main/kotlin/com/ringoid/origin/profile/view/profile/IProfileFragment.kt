package com.ringoid.origin.profile.view.profile

import com.ringoid.utility.ICommunicator

internal interface IProfileFragment : ICommunicator {

    fun onCreateImage()
    fun onDeleteImage()
}
