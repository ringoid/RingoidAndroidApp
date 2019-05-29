package com.ringoid.origin.feed.view.lmm.like

import android.os.Bundle
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.LikeFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.TRANSFER_PROFILE
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedFragment
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LmmNavTab
import com.ringoid.origin.view.particles.PARTICLE_TYPE_LIKE
import com.ringoid.utility.communicator
import timber.log.Timber

class LikesFeedFragment : BaseLmmFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun instantiateFeedAdapter(): BaseLmmAdapter =
        LikeFeedAdapter().apply {
            onLikeImageListener = { model: ProfileImageVO, _ /** feed item position */: Int ->
                if (!connectionManager.isNetworkAvailable()) {
                    noConnection(this@LikesFeedFragment)
                } else {
                    Timber.v("${if (model.isLiked) "L" else "Unl"}iked image: ${model.image}")
                    vm.onLike(profileId = model.profileId, imageId = model.image.id, isLiked = model.isLiked)
                }
            }
        }

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            else -> null
        }

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.LIKES

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is TRANSFER_PROFILE -> {
                        val profileId = (newState.residual as TRANSFER_PROFILE).profileId
                        val discarded = onDiscardProfileState(profileId)  // discard profile on transfer
                        communicator(ILmmFragment::class.java)?.transferProfile(discarded, LmmNavTab.MATCHES)
                    }
                }
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (viewLifecycleOwner) {
            observe(vm.pushNewLike) { communicator(IBaseMainActivity::class.java)?.showParticleAnimation(PARTICLE_TYPE_LIKE) }
        }
    }
}
