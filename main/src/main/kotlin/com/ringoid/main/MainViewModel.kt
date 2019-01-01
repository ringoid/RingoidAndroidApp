package com.ringoid.main

import android.app.Application
import com.ringoid.origin.view.main.BaseMainViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(app: Application) : BaseMainViewModel(app)
