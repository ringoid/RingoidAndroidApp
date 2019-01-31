package com.ringoid.origin.view.main

import androidx.recyclerview.widget.RecyclerView
import com.ringoid.utility.ICommunicator

interface IBaseMainActivity : ICommunicator {

    val imagesViewPool: RecyclerView.RecycledViewPool

    fun isNewUser(): Boolean
}
