package com.ringoid.origin.feed.view.lmm.match

import android.app.Application
import com.ringoid.origin.feed.view.FeedViewModel
import javax.inject.Inject

class MatchesFeedViewModel @Inject constructor(app: Application) : FeedViewModel(app) {

    override fun getFeed() {
        // TODO: get only matches part of lmm here
    }

    override fun getFeedName(): String = "messages"
}
