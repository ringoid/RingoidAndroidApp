package com.ringoid.origin.profile.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BottomSheet
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.origin.profile.R
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.dialog_delete_user_profile_image.*

@BottomSheet(true)
class DeleteUserProfileImageDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "DeleteUserProfileImage_tag"

        private const val BUNDLE_KEY_NEED_WARN = "bundle_key_need_warn"

        fun newInstance(needWarn: Boolean): DeleteUserProfileImageDialog =
            DeleteUserProfileImageDialog().apply {
                arguments = Bundle().apply { putBoolean(BUNDLE_KEY_NEED_WARN, needWarn) }
            }
    }

    private var needWarn: Boolean = false

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_delete_user_profile_image

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        needWarn = arguments?.getBoolean(BUNDLE_KEY_NEED_WARN) ?: false
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_delete_image.clicks().compose(clickDebounce()).subscribe { onImageDelete() }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        close()
    }

    // ------------------------------------------
    private fun close() {
        communicator(IDeleteUserProfileImageActivity::class.java)?.onClose()
    }

    // ------------------------------------------
    private fun onImageDelete() {
        fun deleteImageAndClose() {
            communicator(IDeleteUserProfileImageActivity::class.java)?.onImageDelete()
            close()
        }

        if (needWarn) {
            Dialogs.showTextDialog(activity, titleResId = OriginR_string.profile_dialog_image_delete_title,
                descriptionResId = OriginR_string.common_uncancellable,
                positiveBtnLabelResId = OriginR_string.button_delete, negativeBtnLabelResId = OriginR_string.button_cancel,
                positiveListener = { dialog, _ -> dialog.dismiss() ; deleteImageAndClose() },
                negativeListener = { dialog, _ -> dialog.dismiss() ; onCancel(dialog)})
        } else {
            deleteImageAndClose()
        }
    }
}
