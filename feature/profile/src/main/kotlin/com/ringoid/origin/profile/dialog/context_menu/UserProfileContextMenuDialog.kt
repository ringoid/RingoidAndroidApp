package com.ringoid.origin.profile.dialog.context_menu

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.origin.profile.R
import com.ringoid.utility.ValueUtils.atCharSocialId
import com.ringoid.utility.changeVisibility
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

        with (spm.getUserProfileProperties()) {
            socialInstagram.takeIf { it.isNotBlank() }?.let { instagramUserId ->
                btn_open_social_instagram.text = String.format(resources.getString(OriginR_string.profile_button_open_social_instagram, atCharSocialId(instagramUserId)))
            } ?: run { btn_open_social_instagram.changeVisibility(isVisible = false) }

            socialTikTok.takeIf { it.isNotBlank() }?.let { tiktokUserId ->
                btn_open_social_tiktok.text = String.format(resources.getString(OriginR_string.profile_button_open_social_tiktok, atCharSocialId(tiktokUserId)))
            } ?: run { btn_open_social_tiktok.changeVisibility(isVisible = false) }
        }

        if (BuildConfig.DEBUG) {
            with (btn_delete_image_debug) {
                changeVisibility(isVisible = true)
                clicks().compose(clickDebounce()).subscribe {
                    communicator(IUserProfileContextMenuActivity::class.java)?.onDeleteImageDebug()
                    close()
                }
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        close()
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
        communicator(IUserProfileContextMenuActivity::class.java)?.onDeleteImage()
        close()
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
