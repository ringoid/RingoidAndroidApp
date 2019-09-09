package com.ringoid.origin.view.base.theme

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.style.StyleSharedPrefsManager
import javax.inject.Inject

/**
 * Only exposes access to [StyleSharedPrefsManager]. Use rarely, where such access needed.
 */
abstract class ThemedBaseViewModel(app: Application) : BaseViewModel(app) {

    @Inject protected lateinit var styleSpm: StyleSharedPrefsManager
}
