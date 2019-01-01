package com.ringoid.origin.imagepreview.view

import com.ringoid.base.view.BaseFragment

class ImagePreviewFragment : BaseFragment() {

    companion object {
        const val TAG = "ImagePreviewFragment_tag"

        fun newInstance(): ImagePreviewFragment = ImagePreviewFragment()
    }

    override fun getLayoutId(): Int = 0
}
