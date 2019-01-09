package com.ringoid.origin.profile.view.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.profile.R
import com.ringoid.origin.view.adapter.ImagePagerAdapter
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.utility.snackbar
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver
import kotlinx.android.synthetic.main.fragment_profile.*
import timber.log.Timber

class ProfileFragment : BaseFragment<ProfileFragmentViewModel>() {

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }

    private val imagePreviewReceiver = CropIwaResultReceiver()

    override fun getVmClass(): Class<ProfileFragmentViewModel> = ProfileFragmentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile

    // --------------------------------------------------------------------------------------------
    private lateinit var imagesAdapter: ImagePagerAdapter

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagesAdapter = ProfileImagePagerAdapter(fm = childFragmentManager,
            emptyInput = EmptyFragment.Companion.Input(emptyTextResId = R.string.profile_empty_images))
        imagePreviewReceiver.apply {
            register(context)
            setListener(object : CropIwaResultReceiver.Listener {
                override fun onCropFailed(e: Throwable) {
                    Timber.e(e, "Image crop has failed")
                    view?.let { snackbar(it, R.string.error_crop_image) }
                }

                override fun onCropSuccess(croppedUri: Uri) {
                    Timber.v("Image cropping has succeeded, uri: $croppedUri")
                    // TODO: save image to backend and local cache (repository) and set image
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_images.apply {
            adapter = imagesAdapter
            tabs.setupWithViewPager(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imagePreviewReceiver.unregister(context)
    }
}
