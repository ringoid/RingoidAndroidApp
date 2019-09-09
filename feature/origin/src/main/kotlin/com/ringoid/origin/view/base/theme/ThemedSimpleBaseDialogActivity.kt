package com.ringoid.origin.view.base.theme

import com.ringoid.origin.R
import com.ringoid.origin.style.ThemeUtils

abstract class ThemedSimpleBaseDialogActivity : ThemedSimpleBaseActivity() {

    override fun onBeforeCreate() {
        // discard default App's theme and use dialog theme
        if (ThemeUtils.isDarkTheme(styleSpm)) {
            setTheme(R.style.AppTheme_Dark_SemiTransparent)
        } else {
            setTheme(R.style.AppTheme_Light_SemiTransparent)
        }
    }
}
