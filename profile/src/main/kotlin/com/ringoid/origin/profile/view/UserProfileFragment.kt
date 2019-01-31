package com.ringoid.origin.profile.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.navigation.*
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.origin.profile.R
import com.ringoid.origin.profile.adapter.UserProfileImageAdapter
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import com.ringoid.utility.snackbar
import com.ringoid.widget.view.swipes
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
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
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
            Timber.v("Initializing local receiver for image preview result...")
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

            /**
             * 1. Set listener before register receiver in order to prevent situation when broadcast
             *    is received before the listener has been set, so that broadcast won't be handled.
             *
             * 2. Unregister global app-wide broadcast receiver, because we make it 'local' on UserProfile
             *    screen, using [UserProfileFragment.imagePreviewReceiver] instead of [IBaseRingoidApplication.imagePreviewReceiver].
             *    Once such local receiver is registered, we no longer need global receiver, but we still
             *    should check whether there are any pending result from image preview that should be handled.
             *
             *    Such pending result occasionally happens, when closing ImagePreview screen launches
             *    image cropping task, that finishes before UserProfile screen is opened and this local
             *    receiver is registered.
             */
            register(context)
            Timber.w("image cropping ${globalImagePreviewReceiver()}, result=${globalImagePreviewReceiver()?.hasResult()}")
            globalImagePreviewReceiver()
                ?.also { it.unregister(context) /* global receiver is not needed anymore */ }
                ?.takeIf { it.hasResult() }
                ?.also {
                    Timber.v("Use pending image cropping result from global receiver")
                    val context = activity!!.applicationContext
                    it.getLastError()
                        ?.let { CropIwaResultReceiver.onCropFailed(context, it) }
                        ?: run { it.getLastResult()?.let { CropIwaResultReceiver.onCropCompleted(context, it) } }
                    it.clear()
                }
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
        if (communicator(IBaseMainActivity::class.java)?.isNewUser() != true) {
            vm.getUserImages()
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibtn_add_image.clicks().compose(clickDebounce()).subscribe { vm.onAddImage() }
        ibtn_settings.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/settings") }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            refreshes().compose(clickDebounce()).subscribe { vm.onRefresh() }
            swipes().compose(clickDebounce()).subscribe { vm.onStartRefresh() }
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

        EmptyFragment.Companion.Input(emptyTitleResId = OriginR_string.profile_empty_title, emptyTextResId = OriginR_string.profile_empty_images)
            .apply {
                val emptyFragment = EmptyFragment.newInstance(this)
                childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_empty_container, emptyFragment, EmptyFragment.TAG)
                    .commitNowAllowingStateLoss()
            }
    }

    // ------------------------------------------
    private fun globalImagePreviewReceiver(): IImagePreviewReceiver? =
        this@UserProfileFragment.communicator(IBaseRingoidApplication::class.java)?.imagePreviewReceiver
}
