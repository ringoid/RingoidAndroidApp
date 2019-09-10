package com.ringoid.origin.profile.dialog.context_menu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.profile.context_menu.ContextMenuAction
import com.ringoid.origin.profile.context_menu.ContextMenuExtras
import com.ringoid.origin.view.base.theme.ThemedSimpleBaseDialogActivity

@AppNav("user_profile_context_menu")
class UserProfileContextMenuActivity : ThemedSimpleBaseDialogActivity(), IUserProfileContextMenuActivity {

    companion object {
        private const val BUNDLE_KEY_OUTPUT_DATA = "bundle_key_output_data"
    }

    private var userProfileContextMenuDialog: UserProfileContextMenuDialog? = null

    private lateinit var outputData: Intent

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, outputData)
        finish()
    }

    override fun onAddImage() {
        outputData.putExtra(ContextMenuExtras.EXTRA_ACTION, ContextMenuAction.ADD_IMAGE)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onDeleteImage() {
        outputData.putExtra(ContextMenuExtras.EXTRA_ACTION, ContextMenuAction.DELETE_IMAGE)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onEditProfile() {
        outputData.putExtra(ContextMenuExtras.EXTRA_ACTION, ContextMenuAction.EDIT_PROFILE)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun onEditStatus() {
        outputData.putExtra(ContextMenuExtras.EXTRA_ACTION, ContextMenuAction.EDIT_STATUS)
        setResultExposed(Activity.RESULT_OK, outputData)
    }

    override fun openSocialInstagram() {
        ExternalNavigator.openSocialInstagram(this, instagramUserId = spm.getUserProfileProperties().socialInstagram)
        finish()
    }

    override fun openSocialTiktok() {
        ExternalNavigator.openSocialTiktok(this, tiktokUserId = spm.getUserProfileProperties().socialTikTok)
        finish()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputData = savedInstanceState?.getParcelable(BUNDLE_KEY_OUTPUT_DATA)
            ?: intent.extras?.let { extras -> Intent().putExtras(extras) }
            ?: intent

        savedInstanceState ?: run { createUserProfileContextMenuDialogIfNeed() }
    }

    override fun onBackPressed() {
        onClose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BUNDLE_KEY_OUTPUT_DATA, outputData)
    }

    override fun onDestroy() {
        super.onDestroy()
        userProfileContextMenuDialog = null
    }

    // --------------------------------------------------------------------------------------------
    private fun createUserProfileContextMenuDialogIfNeed() {
        userProfileContextMenuDialog ?: run {
            userProfileContextMenuDialog = UserProfileContextMenuDialog.newInstance()
                .also { it.showNow(supportFragmentManager, UserProfileContextMenuDialog.TAG) }
        }
    }
}
