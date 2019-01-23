package com.ringoid.origin.feed.view.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.IProfile
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.base.IFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.BLOCK_PROFILE
import com.ringoid.origin.feed.view.FeedViewModel
import com.ringoid.origin.feed.view.NO_IMAGES_IN_PROFILE
import com.ringoid.origin.navigation.*
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.collection.EqualRange
import com.ringoid.utility.delay
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class BaseFeedFragment<VM : FeedViewModel, T : IProfile, VH>
    : BaseFragment<VM>() where VH : BaseViewHolder<T>, VH : IFeedViewHolder {

    object InternalNavigator {
        fun openProfileScreen(fragment: Fragment) {
            navigate(fragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}&tabPayload=${Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE}")
        }
    }

    protected lateinit var feedAdapter: BaseFeedAdapter<T, VH>
        private set
    private lateinit var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>

    override fun getLayoutId(): Int = R.layout.fragment_feed

    protected abstract fun createFeedAdapter(): BaseFeedAdapter<T, VH>

    protected abstract fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input?

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            fl_empty_container.changeVisibility(isVisible = false, soft = true)
            swipe_refresh_layout.isRefreshing = false
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> {
                feedAdapter.clear()
                onIdleState()

                getEmptyStateInput(newState.mode)?.let {
                    fl_empty_container.changeVisibility(isVisible = true)
                    val emptyFragment = EmptyFragment.newInstance(it)
                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.fl_empty_container, emptyFragment, EmptyFragment.TAG)
                        .commitNowAllowingStateLoss()
                }
            }
            is ViewState.DONE -> {
                when (newState.residual) {
                    is BLOCK_PROFILE -> {
                        feedAdapter.remove { it.id == (newState.residual as BLOCK_PROFILE).profileId }
                    }
                    is NO_IMAGES_IN_PROFILE -> {
                        Dialogs.showTextDialog(activity,
                            descriptionResId = OriginR_string.feed_explore_dialog_no_user_photo_description,
                            positiveBtnLabelResId = OriginR_string.button_add_photo,
                            negativeBtnLabelResId = OriginR_string.button_later,
                            positiveListener = { _, _ -> vm.onAddImage() })
                        swipe_refresh_layout.isRefreshing = false
                    }
                }
            }
            is ViewState.IDLE -> onIdleState()
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.errorDialog(activity, newState.e)
                onIdleState()
            }
        }
    }

    // ------------------------------------------
    protected fun scrollListToPosition(position: Int) {
        rv_items.smoothScrollToPosition(position)
    }

    protected fun scrollToTopOfItemAtPosition(position: Int) {
        (rv_items.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedAdapter = createFeedAdapter()
            .apply {
                settingsClickListener = { model: T, position: Int, positionOfImage: Int ->
                    val image = model.images[positionOfImage]
                    scrollToTopOfItemAtPosition(position)
                    delay {
                        notifyItemChanged(position, FeedViewHolderHideControls)
                        navigate(this@BaseFeedFragment, path = "/block_dialog?position=$position&profileId=${model.id}&imageId=${image.id}", rc = RequestCode.RC_BLOCK_DIALOG)
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_BLOCK_DIALOG -> {
                if (data == null) {
                    val e = NullPointerException("No output from Block/Report dialog - this is an error!")
                    Timber.e(e) ; throw e
                }

                val position = data.extras!!.getInt("position", 0)

                if (resultCode == Activity.RESULT_OK) {
                    val imageId = data.extras!!.getString("imageId")!!
                    val profileId = data.extras!!.getString("profileId")!!

                    if (data.hasExtra(Extras.OUT_EXTRA_REPORT_REASON)) {
                        val reasonNumber = (data.getIntExtra(Extras.OUT_EXTRA_REPORT_REASON, 0) + 1) * 10
                        vm.onReport(profileId = profileId, imageId = imageId, reasonNumber = reasonNumber)
                    } else {
                        vm.onBlock(profileId = profileId, imageId = imageId)
                    }
                } else {
                    // on dialog dismiss = show controls back
                    feedAdapter.notifyItemChanged(position, FeedViewHolderShowControls)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        trackingBus = TrackingBus(onSuccess = Consumer(vm::onView), onError = Consumer(Timber::e))
        feedAdapter.trackingBus = this@BaseFeedFragment.trackingBus
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_items.apply {
            adapter = feedAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
//            setRecycledViewPool(viewPool)  // TODO: use pool for feeds
//            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setOnRefreshListener { vm.onRefresh() }
        }
    }

    override fun onResume() {
        super.onResume()
        trackingBus.subscribe()
    }

    override fun onPause() {
        super.onPause()
        trackingBus.unsubscribe()
    }
}
