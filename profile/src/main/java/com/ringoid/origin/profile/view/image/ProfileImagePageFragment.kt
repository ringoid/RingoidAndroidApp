package com.ringoid.origin.profile.view.image

import com.ringoid.domain.model.image.Image
import com.ringoid.origin.profile.R
import com.ringoid.origin.view.image.ImagePageFragment

class ProfileImagePageFragment : ImagePageFragment() {

    companion object {
        fun newInstance(image: Image): ProfileImagePageFragment = ProfileImagePageFragment()
    }

    override fun getLayoutId(): Int = R.layout.fragment_profile_image_page

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
