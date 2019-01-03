package com.ringoid.origin.imagepreview.view

import com.ringoid.base.view.BaseFragment

class ImagePreviewFragment : BaseFragment<ImagePreviewViewModel>() {

    companion object {
        const val TAG = "ImagePreviewFragment_tag"

        fun newInstance(): ImagePreviewFragment = ImagePreviewFragment()
    }

    override fun getVmClass(): Class<ImagePreviewViewModel> = ImagePreviewViewModel::class.java

    override fun getLayoutId(): Int = 0
}
