package com.ringoid.origin.rateus.view

import android.app.Application
import com.ringoid.analytics.Analytics
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.rateus.view.RateUsDialog.Companion.RATING_THRESHOLD

class RateUsViewModel(app: Application) : BaseViewModel(app) {

    internal fun cancelRate() {
        analyticsManager.fire(Analytics.RATE_US_ALERT_CANCELLED)
    }

    internal fun sendRating(rating: Int) {
        when {
            rating < RATING_THRESHOLD -> {
                analyticsManager.fire(Analytics.RATE_US_ALERT_FEEDBACK, "rating" to "$rating")
                // TODO: send feedback
            }
            rating >= RATING_THRESHOLD -> {
                analyticsManager.fire(Analytics.RATE_US_ALERT_RATED, "rating" to "$rating")
                // TODO: open rating
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        analyticsManager.fire(Analytics.RATE_US_ALERT_SHOWN)
    }
}
