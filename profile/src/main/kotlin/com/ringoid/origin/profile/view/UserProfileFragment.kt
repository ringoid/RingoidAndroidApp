package com.ringoid.origin.profile.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.AppRes
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.navigation.*
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.origin.profile.R
import com.ringoid.origin.profile.adapter.UserProfileImageAdapter
import com.ringoid.origin.profile.adapter.UserProfileImageViewHolderHideControls
import com.ringoid.origin.profile.adapter.UserProfileImageViewHolderShowControls
import com.ringoid.origin.view.base.ASK_TO_ENABLE_LOCATION_SERVICE
import com.ringoid.origin.view.base.BasePermissionFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.particles.PARTICLE_TYPE_LIKE
import com.ringoid.origin.view.particles.PARTICLE_TYPE_MATCH
import com.ringoid.origin.view.particles.PARTICLE_TYPE_MESSAGE
import com.ringoid.utility.*
import com.ringoid.widget.view.rv.EnhancedPagerSnapHelper
import com.ringoid.widget.view.swipes
import kotlinx.android.synthetic.main.fragment_profile_2.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import timber.log.Timber

class UserProfileFragment : BasePermissionFragment<UserProfileFragmentViewModel>() {

    companion object {
        fun newInstance(): UserProfileFragment = UserProfileFragment()
    }

    private var imageOnViewPortId: String = DomainUtil.BAD_ID
    private var cropImageAfterLogin: Boolean = false
    private var handleRequestToAddImage: Boolean = false

    private lateinit var imagesAdapter: UserProfileImageAdapter
    private lateinit var imagePreloadListener: RecyclerViewPreloader<UserImage>

    override fun getVmClass(): Class<UserProfileFragmentViewModel> = UserProfileFragmentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_profile_2

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            imagesAdapter.notifyItemRangeChanged(0, imagesAdapter.itemCount, UserProfileImageViewHolderShowControls)
            pb_profile.changeVisibility(isVisible = false)
            swipe_refresh_layout.isRefreshing = false
        }
        fun onErrorState() {
            onIdleState()
            showErrorStub(needShow = true)
        }
        fun onLoadingState() {
            pb_profile.changeVisibility(isVisible = BuildConfig.IS_STAGING)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> {
                onIdleState()
                when (newState.mode) {
                    ViewState.CLEAR.MODE_EMPTY_DATA -> showEmptyStub(true)
                    ViewState.CLEAR.MODE_NEED_REFRESH -> showErrorStub(true)
                }
            }
            is ViewState.DONE -> {
                onIdleState()
                updateReferralLabel()
                when (newState.residual) {
                    is ASK_TO_ENABLE_LOCATION_SERVICE -> {
                        val handleCode = (newState.residual as ASK_TO_ENABLE_LOCATION_SERVICE).handleCode
                        when (handleCode) {
                            HC_REFRESH -> vm.onRefresh()  // TODO: use cached
                        }
                    }
                    is REFERRAL_CODE_ACCEPTED -> Dialogs.showTextDialog(activity, title = String.format(resources.getString(OriginR_string.referral_dialog_reward_message), "5"), description = null, positiveBtnLabelResId = OriginR_string.button_ok)
                    is REFERRAL_CODE_DECLINED -> Dialogs.showTextDialog(activity, titleResId = OriginR_string.error_invalid_referral_code, description = null)
                    is REQUEST_TO_ADD_IMAGE -> onAddImageNoPermission()
                }
            }
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> onLoadingState()
            is ViewState.PROGRESS -> {
                imagesAdapter.notifyItemRangeChanged(0, imagesAdapter.itemCount, UserProfileImageViewHolderHideControls)
                onLoadingState()
            }
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onErrorState)
        }
    }

    // ------------------------------------------
    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        payload?.let {
            when (it) {
                Payload.PAYLOAD_PROFILE_LOGIN_IMAGE_ADDED -> { cropImageAfterLogin = true }
                Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE -> {
                    if (isAdded) {
                        onAddImage()
                    } else {
                        /**
                         * Postpone call [onAddImage] until all necessary fields initialized.
                         */
                        handleRequestToAddImage = true
                    }
                }
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagesAdapter = UserProfileImageAdapter(activity!!)
            .apply {
                onInsertListener = { count ->
                    showEmptyStub(needShow = count <= 0)
                    if (count > 1) {  // inserted multiple items
                        // restore position at image on viewport
                        imagesAdapter.getModelAdapterPosition { it.id == imageOnViewPortId }
                            .takeIf { it != DomainUtil.BAD_POSITION }
                            ?.let { scrollToPosition(it) }
                    } else if (count > 0) {  // inserted single item
                        scrollToPosition(0)  // for append: imagesAdapter.itemCount - 1
                    }
                    showDotTabs()
                }
                onRemoveListener = {
                    val empty = imagesAdapter.isEmpty()
                    showEmptyStub(needShow = empty)
                    showDotTabs()
                    vm.onDeleteImage(empty = empty)
                }
            }

        imagePreloadListener = RecyclerViewPreloader(Glide.with(this), imagesAdapter, ViewPreloadSizeProvider<UserImage>(), 10)
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
                showControls(isVisible = true)
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
        with(viewLifecycleOwner) {
            observe(vm.imageBlocked, ::doOnBlockedImage)
            observe(vm.imageCreated, imagesAdapter::prepend)
            observe(vm.imageDeleted, imagesAdapter::remove)
            observe(vm.images, imagesAdapter::submitList)
        }

        showBeginStub()  // empty stub will be replaced after adapter's filled
        globalImagePreviewReceiver()
            ?.doOnError(::onCropFailed)
            ?.doOnSuccess(::onCropSuccess)

        /**
         * Initialization logic: when new user has just logged in, if he (she) has uploaded any image
         * during sign up process, then get that last prepared image, otherwise show empty stub.
         * When user has already logged in and then restarts the app, if it had not been started yet,
         * the app does refresh on Profile screen.
         */
        if (communicator(IBaseMainActivity::class.java)?.isNewUser() == true) {
            if (!cropImageAfterLogin) {
                showEmptyStub(needShow = true)
            }
            globalImagePreviewReceiver()?.subscribe()  // get last prepared image, if any
        } else {
            // refresh Profile screen for already logged in user on a fresh app's start
            permissionManager.askForLocationPermission(this, handleCode = HC_REFRESH)
        }

        if (handleRequestToAddImage) {  // postponed handling to ensure initialization
            handleRequestToAddImage = false
            onAddImage()  // redirect from other screen
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustViews()
        btn_referral.apply {
            updateReferralLabel()
            clicks().compose(clickDebounce()).subscribe {
                if (!spm.hasReferralCode()) {
                    Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.referral_dialog_title,
                        positiveBtnLabelResId = OriginR_string.button_apply,
                        negativeBtnLabelResId = OriginR_string.button_close,
                        positiveListener = { _, _, inputText -> vm.applyReferralCode(code = inputText) })
                }
            }
        }
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
                showControls(isVisible = false)
                val needWarn = ((it as? UserImage)?.numberOfLikes ?: 0) > 0
                navigate(this@UserProfileFragment, path = "/delete_image?imageId=${it.id}&needWarn=$needWarn", rc = RequestCode.RC_DELETE_IMAGE_DIALOG)
            }
        }
        ibtn_settings.clicks().compose(clickDebounce()).subscribe { navigate(this, path = "/settings") }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            refreshes().compose(clickDebounce()).subscribe { onRefresh() }
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
            addOnScrollListener(imagePreloadListener)
        }
        with (tv_app_title) {
            if (BuildConfig.IS_STAGING) {
                isClickable = true
                clicks().compose(clickDebounce()).subscribe {
                    Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.profile_dialog_simulate_particles_title,
                        positiveBtnLabelResId = OriginR_string.button_apply,
                        negativeBtnLabelResId = OriginR_string.button_close,
                        positiveListener = { _, _, inputText -> simulateParticles(inputText?.toInt() ?: 0) })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        globalImagePreviewReceiver()?.dispose()
    }

    // --------------------------------------------------------------------------------------------
    private fun onRefresh() {
        Bus.post(event = BusEvent.RefreshOnProfile)
        if (!connectionManager.isNetworkAvailable()) {
            swipe_refresh_layout.isRefreshing = false
            noConnection(this@UserProfileFragment)
        } else {
            imageOnViewPortId = imageOnViewPort()?.id ?: DomainUtil.BAD_ID
            vm.onRefresh()
        }
    }

    // ------------------------------------------
    private fun onAddImage() {
        /**
         * Asks for location permission, and if granted - callback will then handle
         * opening Gallery to pick image.
         */
        permissionManager.askForLocationPermission(this, handleCode = HC_ADD_IMAGE)
    }

    private fun onAddImageNoPermission() {
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
            positiveListener = { _, _ -> cropImageAfterLogin = true ; onAddImage() },
            negativeListener = { _, _ -> navigate(this@UserProfileFragment, path = "/main?tab=${NavigateFrom.MAIN_TAB_EXPLORE}&tabPayload=${Payload.PAYLOAD_FEED_NEED_REFRESH}") })
    }

    private fun doOnBlockedImage(imageId: String) {
        Dialogs.showTextDialog(activity, titleResId = OriginR_string.profile_dialog_image_blocked_title, descriptionResId = 0,
            positiveBtnLabelResId = OriginR_string.profile_dialog_image_blocked_button,
            negativeBtnLabelResId = OriginR_string.button_close,
            positiveListener = { _, _ -> navigate(this, path = "/webpage?url=${AppRes.WEB_URL_TERMS}") })
    }

    // ------------------------------------------
    private fun showBeginStub() {
        showEmptyStub(needShow = true, input = EmptyFragment.Companion.Input())
    }

    private fun showEmptyStub(needShow: Boolean) {
        showEmptyStub(needShow, input = EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.profile_empty_images))
        ibtn_delete_image.changeVisibility(isVisible = !needShow)
        communicator(IBaseMainActivity::class.java)?.showBadgeWarningOnProfile(isVisible = needShow)
    }

    private fun showErrorStub(needShow: Boolean) {
        showEmptyStub(needShow, input = EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh))
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
        rv_items?.linearLayoutManager()?.findFirstCompletelyVisibleItemPosition()
            ?.takeIf { it != RecyclerView.NO_POSITION }
            ?.let { imagesAdapter.getModel(it) }

    private fun scrollToPosition(position: Int) {
        rv_items?.post { rv_items?.scrollToPosition(position) }
    }

    private fun showControls(isVisible: Boolean) {
        ibtn_add_image.changeVisibility(isVisible = isVisible)
        ibtn_delete_image.changeVisibility(isVisible = isVisible)
        ibtn_settings.changeVisibility(isVisible = isVisible)
        ll_profile_header.changeVisibility(isVisible = isVisible)
        tabs.changeVisibility(isVisible = isVisible)
    }

    // ------------------------------------------
    private fun updateReferralLabel() {
        btn_referral.text = String.format(resources.getString(OriginR_string.profile_label_coins), if (spm.hasReferralCode()) "5" else "0")
    }

    // --------------------------------------------------------------------------------------------
    private fun adjustViews() {
        val density = activity?.getScreenDensity() ?: 4.0f
        if (density <= 3.0f) {
            val margin8 = (AppRes.STD_MARGIN_8 * density / 4.5f).toInt()
            val margin16 = (AppRes.STD_MARGIN_16 * density / 4.5f).toInt()
            with (ibtn_add_image) {
                setPadding(margin16, margin8, margin16, margin8)
            }
            with (ibtn_settings) {
                setPadding(margin16, margin8, margin16, margin8)
            }
        }
    }

    private fun showDotTabs() {
        tabs?.post { tabs?.changeVisibility(isVisible = imagesAdapter.itemCount > 1, soft = true) }
    }

    @DebugOnly
    private fun simulateParticles(count: Int) {
        if (count <= 0) {
            return
        }
        communicator((IBaseMainActivity::class.java))?.let {
            it.showParticleAnimation(id = PARTICLE_TYPE_LIKE, count = count)
            it.showParticleAnimation(id = PARTICLE_TYPE_MATCH, count = count / 10)
            it.showParticleAnimation(id = PARTICLE_TYPE_MESSAGE, count = count / 20)
        }
    }
}
