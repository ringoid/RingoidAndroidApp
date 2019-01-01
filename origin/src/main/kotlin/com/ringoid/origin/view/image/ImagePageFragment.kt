package com.ringoid.origin.view.image

import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.model.image.Image

class ImagePageFragment : BaseFragment() {

    companion object {
        fun newInstance(image: Image): ImagePageFragment = ImagePageFragment()
    }

    override fun getLayoutId(): Int = 0
}
