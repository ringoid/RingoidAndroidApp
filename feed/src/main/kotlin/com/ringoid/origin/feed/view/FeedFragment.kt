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
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.navigate
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.dialog.Dialogs
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class FeedFragment<T : FeedViewModel> : BaseFragment<T>() {

    object InternalNavigator {
        fun openProfileScreen(fragment: Fragment) {
            navigate(fragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}")
        }
    }

    protected lateinit var feedAdapter: FeedAdapter
        private set

    override fun getLayoutId(): Int = R.layout.fragment_feed

    protected open fun createFeedAdapter(): FeedAdapter = FeedAdapter()  // TODO: pass common pool

    protected abstract fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input?

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            swipe_refresh_layout.isRefreshing = false
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> {
                feedAdapter.clear()
                onIdleState()

                getEmptyStateInput(newState.mode)?.let {
                    val emptyFragment = EmptyFragment.newInstance(it)
                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.fl_empty_container, emptyFragment, EmptyFragment.TAG)
                        .commitNowAllowingStateLoss()
                }
            }
            is ViewState.DONE -> {
                when (newState.residual) {
                    NO_IMAGES_IN_PROFILE -> {
                        Dialogs.showTextDialog(activity,
                            descriptionResId = OriginR_string.feed_explore_dialog_no_user_photo_description,
                            positiveBtnLabelResId = OriginR_string.button_later,
                            negativeBtnLabelResId = OriginR_string.button_add_photo,
                            negativeListener = { _, _ -> vm.onAddImage() })
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
        feedAdapter = createFeedAdapter()
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
}
