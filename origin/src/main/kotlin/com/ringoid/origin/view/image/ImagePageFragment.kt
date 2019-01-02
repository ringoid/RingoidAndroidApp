package com.ringoid.origin.view.image

import android.os.Bundle
import android.view.View
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.model.image.Image
import com.ringoid.origin.R
import com.ringoid.origin.R.id.iv_image
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.fragment_image_page.*

open class ImagePageFragment : BaseFragment() {

    companion object {
        const val BUNDLE_KEY_IMAGE = "bundle_key_image"

        fun newInstance(image: Image): ImagePageFragment =
            ImagePageFragment().apply {
                arguments = Bundle().apply { putParcelable(BUNDLE_KEY_IMAGE, image) }
            }
    }

    protected var image: Image? = null

    override fun getLayoutId(): Int = R.layout.fragment_image_page

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { image = it.getParcelable(BUNDLE_KEY_IMAGE) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image?.let { ImageLoader.load(uri = it.uri, imageView = iv_image) }
    }
}
