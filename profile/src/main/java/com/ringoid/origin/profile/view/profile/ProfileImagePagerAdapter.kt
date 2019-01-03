package com.ringoid.origin.profile.view.profile

import androidx.fragment.app.FragmentManager
import com.ringoid.domain.model.image.Image
import com.ringoid.origin.profile.view.image.ProfileImagePageFragment
import com.ringoid.origin.view.adapter.ImagePagerAdapter
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.image.ImagePageFragment

class ProfileImagePagerAdapter(fm: FragmentManager, emptyInput: EmptyFragment.Companion.Input) : ImagePagerAdapter(fm, emptyInput) {

    override fun createImagePageFragment(image: Image): ImagePageFragment<*> =
            ProfileImagePageFragment.newInstance(image)
}
