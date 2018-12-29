package com.ringoid.base.viewmodel

import android.app.Application
import javax.inject.Inject

class SimpleViewModel @Inject constructor(app: Application) : BaseViewModel(app)
