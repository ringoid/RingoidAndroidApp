package com.ringoid.origin.profile.dialog.context_menu

import android.content.Intent
import com.ringoid.origin.view.base.SimpleBaseDialogActivity

class UserProfileContextMenuActivity : SimpleBaseDialogActivity(), IUserProfileContextMenuActivity {

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, Intent().putExtras(intent.extras))
        finish()
    }
}
