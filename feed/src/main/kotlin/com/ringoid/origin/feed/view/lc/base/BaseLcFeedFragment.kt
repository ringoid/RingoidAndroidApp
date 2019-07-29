package com.ringoid.origin.feed.view.lc.base

import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.view.main.LcNavTab

abstract class BaseLcFeedFragment<VM : BaseLcFeedViewModel> : FeedFragment<VM>() {

    protected abstract fun getSourceFeed(): LcNavTab

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_lmm_dialog_no_user_photo_description
}
