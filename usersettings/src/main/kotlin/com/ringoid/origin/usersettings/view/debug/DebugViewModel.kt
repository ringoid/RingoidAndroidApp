package com.ringoid.origin.usersettings.view.debug

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.debug.InvalidAccessTokenRequestUseCase
import com.ringoid.domain.interactor.debug.UnsupportedAppVersionRequestUseCase
import com.ringoid.domain.interactor.debug.WrongParamsRequestUseCase
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import timber.log.Timber
import javax.inject.Inject

class DebugViewModel @Inject constructor(
    private val invalidAccessTokenRequestUseCase: InvalidAccessTokenRequestUseCase,
    private val unsupportedAppVersionRequestUseCase: UnsupportedAppVersionRequestUseCase,
    private val wrongParamsRequestUseCase: WrongParamsRequestUseCase,
    app: Application) : BaseViewModel(app) {

    fun requestWithExpiredAccessToken() {
        invalidAccessTokenRequestUseCase.source(params = Params().put("token", "98736c88-b82e-48d2-bf8b-aeab10a663f7"))
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithInvalidAccessToken() {
        invalidAccessTokenRequestUseCase.source(params = Params().put("token", "invalid_token"))
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithServerError() {
        // TODO:
    }

    fun requestWithStaledAppVersion() {
        unsupportedAppVersionRequestUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithWrongParams() {
        wrongParamsRequestUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    /* Misc */
    // --------------------------------------------------------------------------------------------
    private fun handleResult(): CompletableTransformer =
        CompletableTransformer {
            it.doOnSubscribe { viewState.value = ViewState.LOADING }
                .doOnComplete { viewState.value = ViewState.IDLE }
                .doOnError { viewState.value = ViewState.ERROR(it) }
        }

    private fun Completable.handleResult(handler: BaseViewModel): Completable = compose(handleResult())
}
