package com.ringoid.origin.profile.view

import android.os.Bundle
import android.view.View
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.profile.R

class ProfileFragment : BaseFragment() {

    companion object {
        const val TAG = "ProfileFragment_tag"

        fun newInstance(): ProfileFragment = ProfileFragment()
    }

    override fun getLayoutId(): Int = R.layout.fragment_profile

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
