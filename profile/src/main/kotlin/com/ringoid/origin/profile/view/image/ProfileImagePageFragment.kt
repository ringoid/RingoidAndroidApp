package com.ringoid.origin.profile.view.image

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.profile.R
import com.ringoid.origin.profile.view.profile.IMAGE_DELETED
import com.ringoid.origin.profile.view.profile.IProfileFragment
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.image.ImagePageFragment
import com.ringoid.utility.*
import kotlinx.android.synthetic.main.fragment_profile_image_page.*

class ProfileImagePageFragment : ImagePageFragment<ProfileImagePageViewModel>() {

    companion object {
        fun newInstance(image: IImage): ProfileImagePageFragment =
            ProfileImagePageFragment().apply {
                arguments = Bundle().apply { putParcelable(BUNDLE_KEY_IMAGE, image) }
            }
    }

    override fun getVmClass(): Class<ProfileImagePageViewModel> = ProfileImagePageViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile_image_page

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_image.changeVisibility(isVisible = false)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_image.changeVisibility(isVisible = true)
            is ViewState.DONE -> {
                when (newState.residual) {
                    IMAGE_DELETED -> {
                        snackbar(view, R.string.profile_image_deleted)
                        communicator(IProfileFragment::class.java)?.onDeleteImage()
                        onIdleState()
                    }
                }
            }
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.showTextDialog(activity, titleResId = R.string.error_common, description = "DL TEXT FROM URL")
                onIdleState()
            }
        }
    }

    // ------------------------------------------
    override fun notifyUpdate(image: IImage) {
        super.notifyUpdate(image)
        // TODO: reflect item change event - set number of likes
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibtn_delete_image.touchExtend().clicks().compose(clickDebounce()).subscribe {
            Dialogs.showTextDialog(activity, titleResId = R.string.profile_dialog_delete_image_title,
                descriptionResId = R.string.profile_dialog_delete_image_description,
                positiveBtnLabelResId = R.string.button_delete,
                negativeBtnLabelResId = R.string.button_cancel,
                positiveListener = { _, _ -> vm.deleteImage(image!!.id) })
        }
    }
}
