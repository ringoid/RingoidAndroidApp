package com.ringoid.origin.profile.view.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.profile.R
import com.ringoid.origin.view.adapter.ImagePagerAdapter
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.snackbar
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver
import kotlinx.android.synthetic.main.fragment_profile.*
import timber.log.Timber

class ProfileFragment : BaseFragment<ProfileFragmentViewModel>(), IProfileFragment {

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }

    private lateinit var imagesAdapter: ImagePagerAdapter
    private val imagePreviewReceiver = CropIwaResultReceiver()

    override fun getVmClass(): Class<ProfileFragmentViewModel> = ProfileFragmentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_profile.changeVisibility(isVisible = false)
            swipe_refresh_layout.isRefreshing = false
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_profile.changeVisibility(isVisible = true)
            is ViewState.DONE -> {
                when (newState.residual) {
                    IMAGE_CREATED -> {
                        snackbar(view, R.string.profile_image_created)
                        onCreateImage()
                    }
                }
            }
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.showTextDialog(activity, titleResId = R.string.error_common, description = "DL TEXT FROM URL")
                onIdleState()
            }
        }
    }

    // ------------------------------------------
    override fun onCreateImage() {
        // TODO: move to newly added image page
        imagesAdapter.notifyDataSetChanged()
    }

    override fun onDeleteImage() {
        imagesAdapter.notifyDataSetChanged()
    }

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
                    // TODO: save image local cache (repository) and set image
                    vm.uploadImage(uri = croppedUri)
                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.images.observe(viewLifecycleOwner, Observer { imagesAdapter.set(it) })
        vm.getUserImages()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.apply {
            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setOnRefreshListener { vm.getUserImages() }
        }
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
