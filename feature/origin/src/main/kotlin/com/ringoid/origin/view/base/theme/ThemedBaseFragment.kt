package com.ringoid.origin.view.base.theme

import com.ringoid.base.view.BaseFragment
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.style.StyleSharedPrefsManager
import javax.inject.Inject

/**
 * Only exposes access to [StyleSharedPrefsManager]. Use rarely, where such access needed.
 */
abstract class ThemedBaseFragment<T : BaseViewModel> : BaseFragment<T>() {

    @Inject protected lateinit var styleSpm: StyleSharedPrefsManager
}
