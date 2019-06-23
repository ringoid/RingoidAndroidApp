package com.ringoid.base.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.livedata.ActiveMutableLiveData
import com.ringoid.base.manager.analytics.AnalyticsManager
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.GetUserAccessTokenUseCase
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.manager.ISharedPrefsManager
import com.uber.autodispose.lifecycle.autoDisposable
import leakcanary.LeakSentry
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

abstract class BaseViewModel(app: Application) : AutoDisposeViewModel(app) {

    protected val app: IBaseRingoidApplication by lazy { app as IBaseRingoidApplication }
    protected val context: Context by lazy { app.applicationContext }

    @Inject protected lateinit var getUserAccessTokenUseCase: GetUserAccessTokenUseCase
    @Inject protected lateinit var actionObjectPool: IActionObjectPool
    @Inject protected lateinit var analyticsManager: AnalyticsManager
    @Inject protected lateinit var connectionManager: IConnectionManager
    @Inject protected lateinit var spm: ISharedPrefsManager

    val accessToken: MutableLiveData<LiveEvent<AccessToken?>> by lazy { MutableLiveData<LiveEvent<AccessToken?>>() }
    val viewState: MutableLiveData<ViewState> by lazy { ActiveMutableLiveData<ViewState>() }
    val oneShot: MutableLiveData<LiveEvent<Any?>> by lazy { MutableLiveData<LiveEvent<Any?>>() }

    protected var isStopped = false
        private set
    private var userVisibilityHint: Boolean = false

    // --------------------------------------------------------------------------------------------
    fun getAccessToken() {
        accessToken.value = LiveEvent(spm.accessToken())
    }

    fun obtainAccessToken() {
        getUserAccessTokenUseCase.source(Params.EMPTY)
            .autoDisposable(this)
            .subscribe({ accessToken.value = LiveEvent(it) },
                       { accessToken.value = LiveEvent(null) })
    }

    // ------------------------------------------
    /**
     * @see [BaseFragment.onBeforeTabSelect].
     */
    open fun onBeforeTabSelect() {
        // override in subclasses
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    open fun onCreate(savedInstanceState: Bundle?) {
        // override in subclasses
    }

    /**
     * Called on fresh start of the [Activity] or [Fragment], not after recreate with state restore.
     */
    open fun onFreshStart() {
        // override in subclasses
    }

    open fun onStart() {
        isStopped = false
        // override in subclasses
    }

    open fun onResume() {
        // override in subclasses
    }

    open fun onPause() {
        // override in subclasses
    }

    open fun onSaveInstanceState(outState: Bundle) {
        // override in subclasses
    }

    open fun onStop() {
        isStopped = true
        // override in subclasses
    }

    open fun onDestroy() {
        // override in subclasses
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeFromBusEvents()
        LeakSentry.refWatcher.watch(this)
    }

    /* Visibility */
    // --------------------------------------------------------------------------------------------
    protected fun getUserVisibleHint(): Boolean = userVisibilityHint

    open fun setUserVisibleHint(isVisibleToUser: Boolean): Boolean =
        setUserVisibleHintInternal(isVisibleToUser)

    internal fun setUserVisibleHintInternal(isVisibleToUser: Boolean): Boolean {
        val changed = userVisibilityHint != isVisibleToUser
        userVisibilityHint = isVisibleToUser  // only set flag without side effects
        return changed
    }

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    internal fun subscribeOnBusEvents() {
        Bus.subscribeOnBusEvents(subscriber = this)
    }

    internal fun unsubscribeFromBusEvents() {
        if (Bus.isSubscribed(subscriber = this)) {
            Bus.unsubscribeFromBusEvents(subscriber = this)
        }
    }

    // to prevent from crash - no subscribe methods found in subscriber class
    @Subscribe internal fun onEventStub(event: BusEvent.Stub) {}
}
