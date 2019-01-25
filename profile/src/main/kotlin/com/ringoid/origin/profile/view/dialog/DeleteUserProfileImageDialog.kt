package com.ringoid.origin.profile.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.profile.R
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_delete_user_profile_image.*

@BottomSheet(true)
class DeleteUserProfileImageDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "DeleteUserProfileImage_tag"

        fun newInstance(): DeleteUserProfileImageDialog = DeleteUserProfileImageDialog()
    }

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_delete_user_profile_image

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_delete_image.clicks().compose(clickDebounce()).subscribe {
            communicator(IDeleteUserProfileImageActivity::class.java)?.onImageDelete()
            close()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        close()
    }

    // ------------------------------------------
    private fun close() {
        communicator(IDeleteUserProfileImageActivity::class.java)?.onClose()
    }
}
