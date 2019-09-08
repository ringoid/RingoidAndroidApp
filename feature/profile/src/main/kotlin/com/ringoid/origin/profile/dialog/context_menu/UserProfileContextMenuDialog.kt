package com.ringoid.origin.profile.dialog.context_menu

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.origin.profile.R
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_user_profile_context_menu.*

@BottomSheet(true)
class UserProfileContextMenuDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "UserProfileContextMenu_tag"

        fun newInstance(): UserProfileContextMenuDialog = UserProfileContextMenuDialog()
    }

    override fun getLayoutId(): Int = R.layout.dialog_user_profile_context_menu

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_add_image.clicks().compose(clickDebounce()).subscribe { onAddImage() }
        btn_delete_image.clicks().compose(clickDebounce()).subscribe { onDeleteImage() }
        btn_edit_profile.clicks().compose(clickDebounce()).subscribe { onEditProfile() }
        btn_edit_status.clicks().compose(clickDebounce()).subscribe { onEditStatus() }
        btn_open_social_instagram.clicks().compose(clickDebounce()).subscribe { openSocialInstagram() }
        btn_open_social_tiktok.clicks().compose(clickDebounce()).subscribe { openSocialTiktok() }
    }

    // ------------------------------------------
    private fun close() {
        communicator(IUserProfileContextMenuActivity::class.java)?.onClose()
    }

    // ------------------------------------------
    private fun onAddImage() {
        communicator(IUserProfileContextMenuActivity::class.java)?.onAddImage()
        close()
    }

    private fun onDeleteImage() {
        fun deleteImageAndClose() {
            communicator(IUserProfileContextMenuActivity::class.java)?.onDeleteImage()
            close()
        }

        Dialogs.showTextDialog(activity,
            titleResId = OriginR_string.profile_dialog_image_delete_title,
            descriptionResId = OriginR_string.common_uncancellable,
            positiveBtnLabelResId = OriginR_string.button_delete,
            negativeBtnLabelResId = OriginR_string.button_cancel,
            positiveListener = { dialog, _ -> dialog.dismiss() ; deleteImageAndClose() },
            negativeListener = { dialog, _ -> dialog.dismiss() ; onCancel(dialog)})
    }

    private fun onEditProfile() {
        communicator(IUserProfileContextMenuActivity::class.java)?.onEditProfile()
        close()
    }

    private fun onEditStatus() {
        communicator(IUserProfileContextMenuActivity::class.java)?.onEditStatus()
        close()
    }

    private fun openSocialInstagram() {
        communicator(IUserProfileContextMenuActivity::class.java)?.openSocialInstagram()
        close()
    }

    private fun openSocialTiktok() {
        communicator(IUserProfileContextMenuActivity::class.java)?.openSocialTiktok()
        close()
    }
}
