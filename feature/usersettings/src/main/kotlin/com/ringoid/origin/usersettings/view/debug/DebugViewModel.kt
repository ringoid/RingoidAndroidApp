package com.ringoid.origin.usersettings.view.debug

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.debug.*
import com.ringoid.utility.DebugOnly
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

    private val completeOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    internal fun completeOneShot(): LiveData<OneShot<Boolean>> = completeOneShot

    // --------------------------------------------------------------------------------------------
    fun debugHandleErrorStream() {
        debugHandleErrorDoublestreamUseCase.source()
//        debugHandleErrorStreamUseCase.source()
//        debugHandleErrorDownstreamUseCase.source()
//        debugHandleErrorMultistreamUseCase.source()
//        debugHandleErrorUpstreamUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */}, Timber::e)
    }

    // ------------------------------------------
    fun requestWithCommitActionsFailAllRetries() {
        commitActionsFailUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */}, Timber::e)
    }

    fun requestWithExpiredAccessToken() {
//        invalidAccessTokenRequestUseCase.source(params = Params().put("token", "98736c88-b82e-48d2-bf8b-aeab10a663f7"))
//            .handleResult()
//            .autoDisposable(this)
//            .subscribe({ /* no-op */ }, Timber::e)

        debugInvalidAccessTokenRequestUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithFailAllRetries() {
        requestFailUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithFailNTimesBeforeSuccess(n: Int) {
        requestRetryNTimesUseCase.source(params = Params().put("count", n))
            .doOnComplete { completeOneShot.value = OneShot(true) }
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithInvalidAccessToken() {
        invalidAccessTokenRequestUseCase.source(params = Params().put("token", "invalid_token"))
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithNeedToRepeatAfterDelay(delay: Long /* in seconds */) {
        requestRepeatAfterDelayUseCase.source(params = Params().put("delay", delay))
            .doOnComplete { completeOneShot.value = OneShot(true) }
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithNotSuccessResponse() {
        debugNotSuccessRequestUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWith404Response() {
        debugResponseWith404UseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithServerError() {
        debugServerErrorCauseRequestUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithStaledAppVersion() {
        unsupportedAppVersionRequestUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)

//        debugUnsupportedAppVersionRequestUseCase.source()
//            .handleResult()
//            .autoDisposable(this)
//            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithTimeOutResponse() {
        debugTimeOutRequestUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    fun requestWithWrongParams() {
        wrongParamsRequestUseCase.source()
            .handleResult()
            .autoDisposable(this)
            .subscribe({ /* no-op */ }, Timber::e)
    }

    /* Misc */
    // --------------------------------------------------------------------------------------------
    private fun handleResultTransformer(): CompletableTransformer =
        CompletableTransformer {
            it.doOnSubscribe { viewState.value = ViewState.LOADING }  // DEBUG: common progress
              .doOnComplete { viewState.value = ViewState.IDLE }  // DEBUG: common success
              .doOnError { viewState.value = ViewState.ERROR(it) }  // DEBUG: common error
        }

    private fun Completable.handleResult() = compose(handleResultTransformer())
}
