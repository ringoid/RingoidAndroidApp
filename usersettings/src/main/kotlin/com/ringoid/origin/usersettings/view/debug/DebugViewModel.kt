package com.ringoid.origin.usersettings.view.debug

import android.app.Application
import com.ringoid.base.view.Residual
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.debug.*
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import timber.log.Timber
import javax.inject.Inject

class DebugViewModel @Inject constructor(
    private val invalidAccessTokenRequestUseCase: InvalidAccessTokenRequestUseCase,
    private val requestRepeatAfterDelayUseCase: RequestRepeatAfterDelayUseCase,
    private val requestRetryNTimesUseCase: RequestRetryNTimesUseCase,
    private val unsupportedAppVersionRequestUseCase: UnsupportedAppVersionRequestUseCase,
    private val wrongParamsRequestUseCase: WrongParamsRequestUseCase,
    private val debugInvalidAccessTokenRequestUseCase: DebugInvalidAccessTokenRequestUseCase,
    private val debugNotSuccessRequestUseCase: DebugNotSuccessRequestUseCase,
    private val debugServerErrorCauseRequestUseCase: DebugServerErrorCauseRequestUseCase,
    private val debugTimeOutRequestUseCase: DebugTimeOutRequestUseCase,
    private val debugUnsupportedAppVersionRequestUseCase: DebugUnsupportedAppVersionRequestUseCase,
    app: Application) : BaseViewModel(app) {

    fun requestWithExpiredAccessToken() {
//        invalidAccessTokenRequestUseCase.source(params = Params().put("token", "98736c88-b82e-48d2-bf8b-aeab10a663f7"))
//            .handleResult(this)
//            .autoDisposable(this)
//            .subscribe({ /* no-op */ }, Timber::e)

        debugInvalidAccessTokenRequestUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithFailNTimesBeforeSuccess(n: Int) {
        requestRetryNTimesUseCase.source(params = Params().put("count", n))
            .doOnComplete { viewState.value = ViewState.DONE(Residual()) }
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithInvalidAccessToken() {
        invalidAccessTokenRequestUseCase.source(params = Params().put("token", "invalid_token"))
            .doOnComplete { viewState.value = ViewState.DONE(Residual()) }
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithNeedToRepeatAfterDelay(delay: Long /* in seconds */) {
        requestRepeatAfterDelayUseCase.source(params = Params().put("delay", delay))
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithNotSuccessResponse() {
        debugNotSuccessRequestUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithServerError() {
        debugServerErrorCauseRequestUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithStaledAppVersion() {
        unsupportedAppVersionRequestUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)

//        debugUnsupportedAppVersionRequestUseCase.source()
//            .handleResult(this)
//            .autoDisposable(this)
//            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithTimeOutResponse() {
        debugTimeOutRequestUseCase.source()
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
