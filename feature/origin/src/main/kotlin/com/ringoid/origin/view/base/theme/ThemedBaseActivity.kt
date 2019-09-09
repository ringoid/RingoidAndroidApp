package com.ringoid.origin.view.base.theme

import com.ringoid.base.view.BaseActivity
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.style.StyleSharedPrefsManager
import com.ringoid.origin.style.ThemeId
import com.ringoid.origin.style.ThemeUtils
import javax.inject.Inject

/**
 * Applies current theme to [BaseActivity]. Exposes access to [StyleSharedPrefsManager].
 * Should be used everywhere instead of [BaseActivity], otherwise subclass of [BaseActivity]
 * won't get theme updates, that could be triggered in app.
 */
abstract class ThemedBaseActivity<T : BaseViewModel> : BaseActivity<T>() {

    @Inject protected lateinit var styleSpm: StyleSharedPrefsManager

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onBeforeCreate() {
        styleSpm.getThemeId().takeIf { it != ThemeId.UNKNOWN }?.let { setTheme(ThemeUtils.getThemeById(it)) }
    }
}
