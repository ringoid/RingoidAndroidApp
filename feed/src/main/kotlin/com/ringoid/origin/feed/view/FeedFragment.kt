package com.ringoid.origin.feed.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.OriginR_array
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.collection.EqualRange
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class FeedFragment<T : FeedViewModel> : BaseFragment<T>() {

    object InternalNavigator {
        fun openProfileScreen(fragment: Fragment) {
            navigate(fragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}&tabPayload=${NavigateFrom.PAYLOAD_PROFILE_ADD_IMAGE}")
        }
    }

    protected lateinit var feedAdapter: FeedAdapter
        private set
    private lateinit var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>

    override fun getLayoutId(): Int = R.layout.fragment_feed

    protected open fun createFeedAdapter(): BaseFeedAdapter<*, *> = FeedAdapter()  // TODO: pass common pool

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
                            positiveBtnLabelResId = OriginR_string.button_later,
                            negativeBtnLabelResId = OriginR_string.button_add_photo,
                            negativeListener = { _, _ -> vm.onAddImage() })
                        swipe_refresh_layout.isRefreshing = false
                    }
                }
            }
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> swipe_refresh_layout.isRefreshing = true
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.showTextDialog(activity, titleResId = com.ringoid.origin.R.string.error_common, description = "DL TEXT FROM URL")
                onIdleState()
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedAdapter = (createFeedAdapter() as FeedAdapter)
            .apply {
                onLikeImageListener = { model: ProfileImageVO, _ ->
                    Timber.i("${if (model.isLiked) "L" else "Unl"}iked image: ${model.image}")
                    vm.onLike(profileId = model.profileId, imageId = model.image.id, isLiked = model.isLiked)
                }
                settingsClickListener = { model: Profile, _, positionOfImage: Int ->
                    Dialogs.showSingleChoiceDialog(activity, resources.getStringArray(OriginR_array.block_profile_array),
                        l = { _, which: Int ->
                            val image = model.images[positionOfImage]
                            when (which) {
                                0 -> vm.onBlock(profileId = model.id, imageId = image.id)
                                1 -> Dialogs.showSingleChoiceDialog(activity, resources.getStringArray(OriginR_array.report_profile_array),
                                    l = { _, number: Int ->
                                        vm.onReport(profileId = model.id, imageId = image.id, reasonNumber = (number + 1) * 10)
                                    })
                            }
                        })
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        trackingBus = TrackingBus(onSuccess = Consumer(vm::onView), onError = Consumer(Timber::e))
        feedAdapter.trackingBus = this@FeedFragment.trackingBus
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
