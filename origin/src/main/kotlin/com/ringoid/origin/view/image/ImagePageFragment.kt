package com.ringoid.origin.view.image

import android.os.Bundle
import android.view.View
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.R
import com.ringoid.origin.view.adapter.ImagePagerAdapter
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.fragment_image_page.*

abstract class ImagePageFragment<T : ImagePageViewModel> : BaseFragment<T>(), ImagePagerAdapter.IImagePage {

    companion object {
        const val BUNDLE_KEY_IMAGE = "bundle_key_image"
    }

    override var adapterPosition: Int = 0
    protected var image: IImage? = null

    override fun getLayoutId(): Int = R.layout.fragment_image_page

    // --------------------------------------------------------------------------------------------
    override fun notifyUpdate(image: IImage) {
        this.image = image  // refresh image model
        image.uri?.let { ImageLoader.load(uri = it, imageView = iv_image) }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { image = it.getParcelable(BUNDLE_KEY_IMAGE) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image?.let { notifyUpdate(it) }
    }
}
