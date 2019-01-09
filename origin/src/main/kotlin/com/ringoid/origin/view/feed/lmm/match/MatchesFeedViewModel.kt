package com.ringoid.origin.view.feed.lmm.match

import android.app.Application
import com.ringoid.domain.interactor.feed.GetNewFacesUseCase
import com.ringoid.origin.view.feed.FeedViewModel
import javax.inject.Inject

class MatchesFeedViewModel @Inject constructor(getNewFacesUseCase: GetNewFacesUseCase, app: Application)
    : FeedViewModel(getNewFacesUseCase, app)
