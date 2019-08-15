package com.ringoid.origin.view.base

import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.origin.R
import com.ringoid.origin.style.ThemeUtils

abstract class SimpleBaseDialogActivity : SimpleBaseActivity() {

    override fun onBeforeCreate() {
        // discard default App's theme and use dialog theme
        if (ThemeUtils.isDarkTheme(spm)) {
            setTheme(R.style.AppTheme_Dark_SemiTransparent)
        } else {
            setTheme(R.style.AppTheme_Light_SemiTransparent)
        }
    }
}
