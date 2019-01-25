package com.ringoid.origin.profile.view.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.utility.delay

@AppNav("delete_image")
class DeleteUserProfileImageActivity : SimpleBaseActivity(), IDeleteUserProfileImageActivity {

    private var deleteUserImageDialog: DeleteUserProfileImageDialog? = null

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, Intent().putExtras(intent.extras))
        delay { finish() }
    }

    override fun onImageDelete() {
        setResultExposed(Activity.RESULT_OK, Intent().putExtras(intent.extras))
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run { createDeleteUserImageDialogIfNeed() }
    }

    override fun onBackPressed() {
        onClose()
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteUserImageDialog = null
    }

    // --------------------------------------------------------------------------------------------
    private fun createDeleteUserImageDialogIfNeed() {
        if (deleteUserImageDialog == null) {
            deleteUserImageDialog = DeleteUserProfileImageDialog.newInstance()
                .also { it.showNow(supportFragmentManager, DeleteUserProfileImageDialog.TAG) }
        }
    }
}
