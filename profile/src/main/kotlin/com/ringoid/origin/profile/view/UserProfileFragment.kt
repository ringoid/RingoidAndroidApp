package com.ringoid.origin.profile.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.AppRes
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.navigation.*
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.origin.profile.R
import com.ringoid.origin.profile.adapter.UserProfileImageAdapter
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.utility.*
import com.ringoid.widget.view.rv.EnhancedPagerSnapHelper
import com.ringoid.widget.view.swipes
import kotlinx.android.synthetic.main.fragment_profile.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import timber.log.Timber

class UserProfileFragment : BaseFragment<UserProfileFragmentViewModel>() {

    companion object {
        fun newInstance(): UserProfileFragment = UserProfileFragment()
    }

    private var imageOnViewPortId: String = DomainUtil.BAD_ID
    private var cropImageAfterLogin: Boolean = false

    private lateinit var imagesAdapter: UserProfileImageAdapter

    override fun getVmClass(): Class<UserProfileFragmentViewModel> = UserProfileFragmentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_profile.changeVisibility(isVisible = false)
            swipe_refresh_layout.isRefreshing = false
        }
        fun onErrorState() {
            onIdleState()
            showErrorStub(needShow = true)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_profile.changeVisibility(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onErrorState)
        }
    }

    // ------------------------------------------
    override fun onTabReselect() {
        super.onTabReselect()
        if (imagesAdapter.itemCount < 2) {
            return
        }

        rv_items.linearLayoutManager()
            ?.let {
                val currentPage = it.findFirstCompletelyVisibleItemPosition()
                val nextPage = currentPage + 1
                it.scrollToPosition(nextPage.takeIf { it >= imagesAdapter.itemCount }?.let { 0 } ?: nextPage)
            }
    }

    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        payload?.let {
            when (it) {
                Payload.PAYLOAD_PROFILE_LOGIN_IMAGE_ADDED -> { cropImageAfterLogin = true }
                Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE -> onAddImage()  // redirect from other screen
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagesAdapter = UserProfileImageAdapter()
            .apply {
                onInsertListener = { count ->
                    showEmptyStub(needShow = count <= 0)
                    if (count > 1) {  // inserted multiple items
                        // restore position at image on viewport
                        imagesAdapter.getModelAdapterPosition { it.id == imageOnViewPortId }
                            .takeIf { it != DomainUtil.BAD_POSITION }
                            ?.let { scrollToPosition(it) }
                    } else if (count > 0) {
                        scrollToPosition(0)  // inserted single item
                    }
                }
                onRemoveListener = {
                    val empty = imagesAdapter.isEmpty()
                    showEmptyStub(needShow = empty)
                    vm.onDeleteImage(empty = empty)
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
        fun onCropFailed(e: Throwable) {
            Timber.e(e, "Image crop has failed")
            view?.let { snackbar(it, OriginR_string.error_crop_image) }
            doOnCropErrorAfterLogin()
        }

        fun onCropSuccess(croppedUri: Uri) {
            Timber.v("Image cropping has succeeded, uri: $croppedUri")
            vm.uploadImage(uri = croppedUri)
            doOnCropSuccessAfterLogin()
        }

        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.apply {
            observe(vm.imageBlocked, ::doOnBlockedImage)
            observe(vm.imageCreated, imagesAdapter::prepend)
            observe(vm.imageDeleted, imagesAdapter::remove)
            observe(vm.images, imagesAdapter::submitList)
        }
        if (communicator(IBaseMainActivity::class.java)?.isNewUser() != true) {
            vm.getUserImages()
        }

        globalImagePreviewReceiver()
            ?.doOnError(::onCropFailed)
            ?.doOnSuccess(::onCropSuccess)
            ?.subscribe()
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibtn_add_image.clicks().compose(clickDebounce())
            .subscribe {
                if (!connectionManager.isNetworkAvailable()) {
                    noConnection(this)
                    return@subscribe
                }
                onAddImage()
            }
        ibtn_delete_image.clicks().compose(clickDebounce()).subscribe {
            if (!connectionManager.isNetworkAvailable()) {
                noConnection(this)
                return@subscribe
            }

            imageOnViewPort()?.let {
                navigate(this@UserProfileFragment, path = "/delete_image?imageId=${it.id}", rc = RequestCode.RC_DELETE_IMAGE_DIALOG)
            }
        }
        ibtn_settings.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/settings") }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            refreshes().compose(clickDebounce()).subscribe {
                imageOnViewPortId = imageOnViewPort()?.id ?: DomainUtil.BAD_ID
                communicator(IBaseMainActivity::class.java)?.onRefreshFeed()
                vm.onRefresh()
            }
            swipes().compose(clickDebounce()).subscribe { vm.onStartRefresh() }
        }
        val snapHelper = EnhancedPagerSnapHelper(duration = 30)
        rv_items.apply {
            adapter = imagesAdapter.also { it.tabsObserver = tabs.adapterDataObserver }
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            snapHelper.attachToRecyclerView(this)
            tabs.attachToRecyclerView(this, snapHelper)
            setHasFixedSize(true)
            setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        }

        showEmptyStub(needShow = true)  // empty stub will be replaced on adapter's filled
    }

    override fun onDestroy() {
        super.onDestroy()
        globalImagePreviewReceiver()?.dispose()
    }

    // --------------------------------------------------------------------------------------------
    private fun onAddImage() {
        ExternalNavigator.openGalleryToGetImageFragment(this)
    }

    private fun doOnCropErrorAfterLogin() {
        if (!cropImageAfterLogin) {
            return
        }

        cropImageAfterLogin = false  // ask only once per session
        showEmptyStub(needShow = true)
    }

    private fun doOnCropSuccessAfterLogin() {
        if (!cropImageAfterLogin) {
            return
        }

        cropImageAfterLogin = false  // ask only once per session
        Dialogs.showTextDialog(activity, titleResId = OriginR_string.profile_dialog_image_another_title, descriptionResId = 0,
            positiveBtnLabelResId = OriginR_string.profile_dialog_image_another_button_add,
            negativeBtnLabelResId = OriginR_string.profile_dialog_image_another_button_cancel,
            positiveListener = { _, _ -> onAddImage() },
            negativeListener = { _, _ -> navigate(this@UserProfileFragment, path = "/main?tab=${NavigateFrom.MAIN_TAB_FEED}") })
    }

    private fun doOnBlockedImage(imageId: String) {
        Dialogs.showTextDialog(activity, titleResId = OriginR_string.profile_dialog_image_blocked_title, descriptionResId = 0,
            positiveBtnLabelResId = OriginR_string.profile_dialog_image_blocked_button,
            negativeBtnLabelResId = OriginR_string.button_close,
            positiveListener = { _, _ -> navigate(this, path = "/webpage?url=${AppRes.WEB_URL_TERMS}") })
    }

    // ------------------------------------------
    private fun showEmptyStub(needShow: Boolean) {
        showEmptyStub(needShow, input = EmptyFragment.Companion.Input(emptyTitleResId = OriginR_string.profile_empty_title, emptyTextResId = OriginR_string.profile_empty_images))
        ibtn_delete_image.changeVisibility(isVisible = !needShow)
        communicator(IBaseMainActivity::class.java)?.showBadgeWarningOnProfile(isVisible = needShow)
    }

    private fun showErrorStub(needShow: Boolean) {
        showEmptyStub(needShow, input = EmptyFragment.Companion.Input(emptyTitleResId = OriginR_string.profile_empty_title, emptyTextResId = OriginR_string.profile_error_stub))
    }

    private fun showEmptyStub(needShow: Boolean, input: EmptyFragment.Companion.Input) {
        fl_empty_container.changeVisibility(isVisible = needShow)
        if (!needShow) {
            return
        }

        input.apply {
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

    private fun imageOnViewPort(): IImage? =
        rv_items.linearLayoutManager()?.findFirstCompletelyVisibleItemPosition()
            ?.takeIf { it != RecyclerView.NO_POSITION }
            ?.let { imagesAdapter.getModel(it) }

    private fun scrollToPosition(position: Int) {
        rv_items.post { rv_items.scrollToPosition(position) }
    }
}
