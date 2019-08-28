package com.ringoid.base.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.AnalyticsManager
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.livedata.ActiveMutableLiveData
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.interactor.user.GetUserAccessTokenUseCase
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.manager.ISharedPrefsManager
import leakcanary.AppWatcher
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

    protected val viewState: MutableLiveData<ViewState> by lazy { ActiveMutableLiveData<ViewState>(ViewState.NO_STATE) }
    fun viewState(): LiveData<ViewState> = viewState

    protected var isStopped = false
        private set
    private var userVisibilityHint: Boolean = false

    // --------------------------------------------------------------------------------------------
    /**
     * @see [BaseFragment.onBeforeTabSelect].
     */
    open fun onBeforeTabSelect() {
        // override in subclasses
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    /**
     * Called when [BaseViewModel] has just been created, it could be either in [Activity.onCreate]
     * or in [Fragment.onActivityCreated] methods. [savedInstanceState] is passed through.
     * If [savedInstanceState] is not null, then it's either recovery after configuration change,
     * or restore after destruction. Thus [BaseViewModel] should then check whether it has some
     * data already cached in-memory, because [BaseViewModel] outlives configuration changes,
     * or it should parse [savedInstanceState] to restore that data, because it's a new [BaseViewModel]
     * instance.
     *
     * @see https://medium.com/androiddevelopers/viewmodels-persistence-onsaveinstancestate-restoring-ui-state-and-loaders-fc7cc4a6c090
     */
    open fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            if (viewState.value == ViewState.NO_STATE) {
                onRecreate(it)
            }
        }
        // override in subclasses
    }

    /**
     * [BaseViewModel] was destroyed and then recreated. It has a chance to restore it's previous
     * state from [savedInstanceState], because all it's in-memory values, such as [LiveData]
     * and so on was permanently lost.
     */
    protected open fun onRecreate(savedInstanceState: Bundle) {
        // override in subclasses
    }

    /**
     * Called on fresh start of the [Activity] or [Fragment], not after recreate with state restore.
     * Basically, it is called in [Activity.onStart] or [Fragment.onStart] if and only if
     * 'savedInstanceState' is null.
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
        AppWatcher.objectWatcher.watch(this)
    }

    // ------------------------------------------
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // override in subclasses
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
