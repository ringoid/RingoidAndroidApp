package com.ringoid.base.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.user.GetUserAccessTokenUseCase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {

    @Inject lateinit var getUserAccessTokenUseCase: GetUserAccessTokenUseCase

    protected var subs: Disposable? = null  // for single subscription
    protected val cs: CompositeDisposable = CompositeDisposable()  // for multiple subscriptions

    val viewState: MutableLiveData<ViewState> by lazy { MutableLiveData<ViewState>() }

    override fun onCleared() {
        super.onCleared()
        subs?.dispose()
        subs = null
        cs.clear()
    }
}
