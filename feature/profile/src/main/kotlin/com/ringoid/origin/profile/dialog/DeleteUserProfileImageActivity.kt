package com.ringoid.origin.profile.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.SimpleBaseDialogActivity
import com.ringoid.utility.DebugOnly

@AppNav("delete_image")
class DeleteUserProfileImageActivity : SimpleBaseDialogActivity(), IDeleteUserProfileImageActivity {

    private var deleteUserImageDialog: DeleteUserProfileImageDialog? = null

    // --------------------------------------------------------------------------------------------
    override fun onClose() {
        setResultExposed(currentResult, Intent().putExtras(intent.extras))
        finish()
    }

    override fun onImageDelete() {
        setResultExposed(Activity.RESULT_OK, Intent().putExtras(intent.extras))
    }

    @DebugOnly
    override fun onImageDeleteDebugAndClose() {
        setResultExposed(Activity.RESULT_OK, Intent().putExtra("debug", true).putExtras(intent.extras))
        finish()
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
            val needWarn = intent.extras?.getString("needWarn")?.toBoolean() ?: false
            deleteUserImageDialog = DeleteUserProfileImageDialog.newInstance(needWarn)
                .also { it.showNow(supportFragmentManager, DeleteUserProfileImageDialog.TAG) }
        }
    }
}
