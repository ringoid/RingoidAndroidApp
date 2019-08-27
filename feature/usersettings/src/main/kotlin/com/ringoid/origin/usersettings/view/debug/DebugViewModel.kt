package com.ringoid.origin.usersettings.view.debug

import android.app.Application
import com.ringoid.base.view.Residual
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.debug.*
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import timber.log.Timber
import javax.inject.Inject

@DebugOnly
class DebugViewModel @Inject constructor(
    @DebugOnly private val commitActionsFailUseCase: CommitActionsFailUseCase,
    @DebugOnly private val invalidAccessTokenRequestUseCase: InvalidAccessTokenRequestUseCase,
    @DebugOnly private val requestFailUseCase: RequestFailUseCase,
    @DebugOnly private val requestRepeatAfterDelayUseCase: RequestRepeatAfterDelayUseCase,
    @DebugOnly private val requestRetryNTimesUseCase: RequestRetryNTimesUseCase,
    @DebugOnly private val unsupportedAppVersionRequestUseCase: UnsupportedAppVersionRequestUseCase,
    @DebugOnly private val wrongParamsRequestUseCase: WrongParamsRequestUseCase,
    @DebugOnly private val debugHandleErrorDoublestreamUseCase: DebugHandleErrorDoublestreamUseCase,
    @DebugOnly private val debugHandleErrorDownstreamUseCase: DebugHandleErrorDownstreamUseCase,
    @DebugOnly private val debugHandleErrorUpstreamUseCase: DebugHandleErrorUpstreamUseCase,
    @DebugOnly private val debugHandleErrorMultistreamUseCase: DebugHandleErrorMultistreamUseCase,
    @DebugOnly private val debugHandleErrorStreamUseCase: DebugHandleErrorStreamUseCase,
    @DebugOnly private val debugInvalidAccessTokenRequestUseCase: DebugInvalidAccessTokenRequestUseCase,
    @DebugOnly private val debugNotSuccessRequestUseCase: DebugNotSuccessRequestUseCase,
    @DebugOnly private val debugResponseWith404UseCase: DebugResponseWith404UseCase,
    @DebugOnly private val debugServerErrorCauseRequestUseCase: DebugServerErrorCauseRequestUseCase,
    @DebugOnly private val debugTimeOutRequestUseCase: DebugTimeOutRequestUseCase,
    @DebugOnly private val debugUnsupportedAppVersionRequestUseCase: DebugUnsupportedAppVersionRequestUseCase,
    app: Application) : BaseViewModel(app) {

    // --------------------------------------------------------------------------------------------
    fun debugHandleErrorStream() {
        debugHandleErrorDoublestreamUseCase.source()
//        debugHandleErrorStreamUseCase.source()
//        debugHandleErrorDownstreamUseCase.source()
//        debugHandleErrorMultistreamUseCase.source()
//        debugHandleErrorUpstreamUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */}, Timber::e)
    }

    // ------------------------------------------
    fun requestWithCommitActionsFailAllRetries() {
        commitActionsFailUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */}, Timber::e)
    }

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

    fun requestWithFailAllRetries() {
        requestFailUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithFailNTimesBeforeSuccess(n: Int) {
        requestRetryNTimesUseCase.source(params = Params().put("count", n))
            .handleResult(this, ViewState.DONE(Residual()))
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithInvalidAccessToken() {
        invalidAccessTokenRequestUseCase.source(params = Params().put("token", "invalid_token"))
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithNeedToRepeatAfterDelay(delay: Long /* in seconds */) {
        requestRepeatAfterDelayUseCase.source(params = Params().put("delay", delay))
            .handleResult(this, ViewState.DONE(Residual()))
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithNotSuccessResponse() {
        debugNotSuccessRequestUseCase.source()
            .handleResult(this)
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWith404Response() {
        debugResponseWith404UseCase.source()
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
    private fun handleResult(completeState: ViewState = ViewState.IDLE): CompletableTransformer =
        CompletableTransformer {
            it.doOnSubscribe { viewState.value = ViewState.LOADING }
                .doOnComplete { viewState.value = completeState }
                .doOnError { viewState.value = ViewState.ERROR(it) }
        }

    private fun Completable.handleResult(handler: BaseViewModel, completeState: ViewState = ViewState.IDLE)
            : Completable = compose(handleResult(completeState))
}
