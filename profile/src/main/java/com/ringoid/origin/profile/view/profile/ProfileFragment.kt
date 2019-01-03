package com.ringoid.origin.profile.view.profile

import android.os.Bundle
import android.view.View
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.profile.R
import com.ringoid.origin.view.adapter.ImagePagerAdapter
import com.ringoid.origin.view.common.EmptyFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment<ProfileFragmentViewModel>() {

    companion object {
        fun newInstance(): ProfileFragment =
            ProfileFragment()
    }

    override fun getVmClass(): Class<ProfileFragmentViewModel> = ProfileFragmentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile

    // --------------------------------------------------------------------------------------------
    private lateinit var imagesAdapter: ImagePagerAdapter

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagesAdapter = ImagePagerAdapter(fm = childFragmentManager,
            emptyInput = EmptyFragment.Companion.Input(emptyTextResId = R.string.profile_empty_images))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_images.apply {
            adapter = imagesAdapter
            tabs.setupWithViewPager(this)
        }
    }
}
