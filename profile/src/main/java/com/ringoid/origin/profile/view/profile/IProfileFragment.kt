package com.ringoid.origin.profile.view.profile

import com.ringoid.utility.ICommunicator

interface IProfileFragment : ICommunicator {

    fun onCreateImage()
    fun onDeleteImage()
}
