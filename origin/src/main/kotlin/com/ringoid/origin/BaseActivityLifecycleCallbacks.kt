package com.ringoid.origin

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.ringoid.utility.manager.LocaleManager

class BaseActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity.application as? BaseRingoidApplication)?.localeManager?.setLocale(activity)
        LocaleManager.resetActivityTitle(activity)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}
