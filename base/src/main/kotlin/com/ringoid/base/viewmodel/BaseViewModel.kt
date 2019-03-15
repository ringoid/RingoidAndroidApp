package com.ringoid.base.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.IBaseRingoidApplication
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.GetUserAccessTokenUseCase
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.repository.ISharedPrefsManager
import com.uber.autodispose.lifecycle.autoDisposable
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

abstract class BaseViewModel(app: Application) : AutoDisposeViewModel(app) {

    protected val app: IBaseRingoidApplication by lazy { app as IBaseRingoidApplication }
    protected val context: Context by lazy { app.applicationContext }

    @Inject protected lateinit var getUserAccessTokenUseCase: GetUserAccessTokenUseCase
    @Inject protected lateinit var actionObjectPool: IActionObjectPool
    @Inject protected lateinit var connectionManager: IConnectionManager
    @Inject protected lateinit var spm: ISharedPrefsManager

    val accessToken: MutableLiveData<AccessToken?> by lazy { MutableLiveData<AccessToken?>() }
    val viewState: MutableLiveData<ViewState> by lazy { MutableLiveData<ViewState>() }
    val oneShot: MutableLiveData<LiveEvent<Any?>> by lazy { MutableLiveData<LiveEvent<Any?>>() }

    // --------------------------------------------------------------------------------------------
    fun obtainAccessToken() {
        getUserAccessTokenUseCase.source(Params.EMPTY)
            .autoDisposable(this)
            .subscribe({ accessToken.value = it }, { accessToken.value = null })
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    /**
     * Called on fresh start of the [Activity] or [Fragment], not after recreate with state restore.
     */
    open fun onFreshStart() {
        // override in subclasses
    }

    open fun onStart() {
        // override in subclasses
    }

    open fun onStop() {
        // override in subclasses
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeFromBusEvents()
    }

    // ------------------------------------------
    open fun setUserVisibleHint(isVisibleToUser: Boolean) {
        // override in subclasses
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
