package com.ringoid.origin.view.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.ringoid.origin.view.base.theme.ThemedSimpleBaseActivity

abstract class BaseDialogHostActivity : ThemedSimpleBaseActivity() {

    protected abstract fun getFragmentTag(): String
    protected abstract fun instantiateFragment(): DialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            instantiateFragment().showNow(supportFragmentManager, getFragmentTag())
        }
    }
}
