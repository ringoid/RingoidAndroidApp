package com.ringoid.origin.profile.dialog.context_menu

import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment

@BottomSheet(true)
class UserProfileContextMenuDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "UserProfileContextMenu_tag"

        fun newInstance(): UserProfileContextMenuDialog = UserProfileContextMenuDialog()
    }

    override fun getLayoutId(): Int = 0
}
