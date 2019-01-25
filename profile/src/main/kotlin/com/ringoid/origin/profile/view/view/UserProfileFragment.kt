package com.ringoid.origin.profile.view.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.navigation.*
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.origin.profile.R
import com.ringoid.origin.profile.view.adapter.UserProfileImageAdapter
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.snackbar
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver
import kotlinx.android.synthetic.main.fragment_profile.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import timber.log.Timber

class UserProfileFragment : BaseFragment<UserProfileFragmentViewModel>() {

    companion object {
        fun newInstance(): UserProfileFragment = UserProfileFragment()
    }

    private var shouldAskToAddAnotherImage: Boolean = false

    private lateinit var imagesAdapter: UserProfileImageAdapter
    private val imagePreviewReceiver = CropIwaResultReceiver()

    override fun getVmClass(): Class<UserProfileFragmentViewModel> = UserProfileFragmentViewModel::class.java

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
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.errorDialog(this, newState.e)
                onIdleState()
            }
        }
    }

    // ------------------------------------------
    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        payload?.let {
            when (it) {
                Payload.PAYLOAD_PROFILE_LOGIN_IMAGE_ADDED -> { shouldAskToAddAnotherImage = true }
                Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE -> vm.onAddImage()
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagesAdapter = UserProfileImageAdapter().apply {
            onDeleteImageListener = { model: UserImage, _ ->
                navigate(this@UserProfileFragment, path = "/delete_image?imageId=${model.id}", rc = RequestCode.RC_DELETE_IMAGE_DIALOG)
            }
            onEmptyImagesListener = ::showEmptyStub
        }

        imagePreviewReceiver.apply {
            register(context)
            setListener(object : CropIwaResultReceiver.Listener {
                override fun onCropFailed(e: Throwable) {
                    Timber.e(e, "Image crop has failed")
                    view?.let { snackbar(it, OriginR_string.error_crop_image) }
                }

                override fun onCropSuccess(croppedUri: Uri) {
                    Timber.v("Image cropping has succeeded, uri: $croppedUri")
                    vm.uploadImage(uri = croppedUri)
                    askToAddAnotherImage()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ExternalNavigator.RC_GALLERY_GET_IMAGE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> navigate(this, path = "/imagepreview", rc = RC_IMAGE_PREVIEW, payload = data)
                }
            }
            RequestCode.RC_DELETE_IMAGE_DIALOG -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        val e = NullPointerException("No output image id from Delete Image dialog - this is an error!")
                        Timber.e(e) ; throw e
                    }

                    vm.deleteImage(id = data.getStringExtra("imageId"))
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.apply {
            observe(vm.imageCreated, imagesAdapter::prepend) { rv_items.post { rv_items.smoothScrollToPosition(0) } }
            observe(vm.imageDeleted, imagesAdapter::remove)
            observe(vm.imageIdChanged, imagesAdapter::updateItemId)
            observe(vm.images, imagesAdapter::submitList)
        }
        vm.getUserImages()
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibtn_add_image.clicks().compose(clickDebounce()).subscribe { vm.onAddImage() }
        ibtn_settings.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/settings") }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setOnRefreshListener { vm.onRefresh() }
        }
        val snapHelper = PagerSnapHelper()
        rv_items.apply {
            adapter = imagesAdapter.also { it.tabsObserver = tabs.adapterDataObserver }
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            snapHelper.attachToRecyclerView(this)
            tabs.attachToRecyclerView(this, snapHelper)
            setHasFixedSize(true)
//            setRecycledViewPool(viewPool)
            setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imagePreviewReceiver.unregister(context)
    }

    // --------------------------------------------------------------------------------------------
    private fun askToAddAnotherImage() {
        if (!shouldAskToAddAnotherImage) {
            return
        }

        shouldAskToAddAnotherImage = false  // ask only once per session
        Dialogs.showTextDialog(activity, titleResId = OriginR_string.profile_dialog_image_another_title, descriptionResId = 0,
            positiveBtnLabelResId = OriginR_string.profile_dialog_image_another_button_add,
            negativeBtnLabelResId = OriginR_string.profile_dialog_image_another_button_cancel,
            positiveListener = { _, _ -> vm.onAddImage() },
            negativeListener = { _, _ -> navigate(this@UserProfileFragment, path = "/main?tab=${NavigateFrom.MAIN_TAB_FEED}") })
    }

    private fun showEmptyStub(needShow: Boolean) {
        fl_empty_container.changeVisibility(isVisible = needShow)
        if (!needShow) {
            return
        }

        EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.profile_empty_images)
            .apply {
                val emptyFragment = EmptyFragment.newInstance(this)
                childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_empty_container, emptyFragment, EmptyFragment.TAG)
                    .commitNowAllowingStateLoss()
            }
    }
}
