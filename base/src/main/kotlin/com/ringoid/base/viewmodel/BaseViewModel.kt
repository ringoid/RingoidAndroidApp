package com.ringoid.base.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.GetUserAccessTokenUseCase
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.repository.ISharedPrefsManager
import com.uber.autodispose.lifecycle.autoDisposable
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject
import kotlin.reflect.KFunction

abstract class BaseViewModel(app: Application) : AutoDisposeViewModel(app) {

    protected val context: Context by lazy { app.applicationContext }

    @Inject lateinit var getUserAccessTokenUseCase: GetUserAccessTokenUseCase
    @Inject lateinit var actionObjectPool: IActionObjectPool
    @Inject lateinit var spm: ISharedPrefsManager

    val accessToken: MutableLiveData<AccessToken?> by lazy { MutableLiveData<AccessToken?>() }
    val navigation: MutableLiveData<KFunction<*>> by lazy { MutableLiveData<KFunction<*>>() }
    val viewState: MutableLiveData<ViewState> by lazy { MutableLiveData<ViewState>() }

    // --------------------------------------------------------------------------------------------
    fun obtainAccessToken() {
        getUserAccessTokenUseCase.source(Params.EMPTY)
            .autoDisposable(this)
            .subscribe({ accessToken.value = it }, { accessToken.value = null })
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
        unsubscribeFromBusEvents()
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
