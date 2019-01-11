package com.ringoid.origin.view.feed

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.model.feed.Feed

abstract class FeedViewModel(app: Application) : BaseViewModel(app) {

    val feed by lazy { MutableLiveData<Feed>() }

    abstract fun getFeed()
}
