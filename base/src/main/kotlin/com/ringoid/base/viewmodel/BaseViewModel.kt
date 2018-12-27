package com.ringoid.base.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState

abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {

    val viewState: MutableLiveData<ViewState> by lazy { MutableLiveData<ViewState>() }
}
