package com.ringoid.base.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.user.GetUserAccessTokenUseCase
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.repository.ISharedPrefsManager
import com.uber.autodispose.AutoDispose.autoDisposable
import javax.inject.Inject
import kotlin.reflect.KFunction

abstract class BaseViewModel(app: Application) : AutoDisposeViewModel(app) {

    @Inject lateinit var getUserAccessTokenUseCase: GetUserAccessTokenUseCase
    @Inject lateinit var spm: ISharedPrefsManager

    val accessToken: MutableLiveData<AccessToken?> by lazy { MutableLiveData<AccessToken?>() }
    val navigation: MutableLiveData<KFunction<*>> by lazy { MutableLiveData<KFunction<*>>() }
    val viewState: MutableLiveData<ViewState> by lazy { MutableLiveData<ViewState>() }

    // --------------------------------------------------------------------------------------------
    fun obtainAccessToken() {
        getUserAccessTokenUseCase.source(Params.EMPTY)
            .`as`(autoDisposable(this))
            .subscribe({ accessToken.value = it }, { accessToken.value = null })
    }
}
