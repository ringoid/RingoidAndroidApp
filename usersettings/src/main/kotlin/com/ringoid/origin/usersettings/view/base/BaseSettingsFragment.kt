package com.ringoid.origin.usersettings.view.base

import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.view.dialog.BigEditTextDialog

abstract class BaseSettingsFragment<VM : BaseSettingsViewModel> : BaseFragment<VM>(), BigEditTextDialog.IBigEditTextDialogDone {

    override fun onDone(text: String, tag: String?) {
        vm.suggestImprovements(text, tag)
    }

    protected fun openSuggestImprovementsDialog() {
        BigEditTextDialog.newInstance(titleResId = OriginR_string.suggest_improvements_title,
            descriptionResId = OriginR_string.suggest_improvements_description,
            btnPositiveResId = OriginR_string.suggest_improvements_positive_button,
            tag = "SuggestFromSettings")
            .show(childFragmentManager, BigEditTextDialog.TAG)
    }
}
