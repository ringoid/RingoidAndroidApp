package com.ringoid.origin.profile.view.image

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.model.image.Image
import com.ringoid.origin.profile.R
import com.ringoid.origin.view.image.ImagePageFragment
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_profile_image_page.*

class ProfileImagePageFragment : ImagePageFragment<ProfileImagePageViewModel>() {

    companion object {
        fun newInstance(image: Image): ProfileImagePageFragment =
            ProfileImagePageFragment().apply {
                arguments = Bundle().apply { putParcelable(BUNDLE_KEY_IMAGE, image) }
            }
    }

    override fun getVmClass(): Class<ProfileImagePageViewModel> = ProfileImagePageViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile_image_page

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibtn_delete_image.clicks().compose(clickDebounce()).subscribe { vm.deleteImage(image!!.id) }
    }
}
