package com.ringoid.origin.feed.view.lc

import android.content.Context
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.GetLcUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.model.feed.Filters
import com.ringoid.domain.model.feed.NoFilters
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.report.log.Report
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates single entry point for data loading for LC feeds and notifies them on arrival.
 * Helps to avoid double-loading of separate LC feeds, basically when some bus event occurs.
 *
 * @note: coordinates only bus events
 */
@Singleton
class LcCoordinator @Inject constructor(
    private val context: Context,
    private val filtersSource: IFiltersSource,
    private val getLcUseCase: GetLcUseCase,
    private val countUserImagesUseCase: CountUserImagesUseCase) {

    interface LcDataListener {
        fun onErrorLcDataLoading(e: Throwable)
        fun onStartLcDataLoading()
    }

    private var filters: Filters = NoFilters
    private val listeners: MutableList<LcDataListener> = mutableListOf()
    private val scopeProvider = LcCoordinatorScopeProvider()

    internal fun init() {
        scopeProvider.activate()
        Bus.subscribeOnBusEvents(subscriber = this)
    }

    internal fun deinit() {
        if (Bus.isSubscribed(subscriber = this)) {
            Bus.unsubscribeFromBusEvents(subscriber = this)
        }
        scopeProvider.dispose()
    }

    // ------------------------------------------
    internal fun registerListener(l: LcDataListener) {
        listeners.add(l)
    }

    internal fun unregisterListener(l: LcDataListener) {
        listeners.remove(l)
    }

    // ------------------------------------------
    internal fun onApplyFilters() {
        // update filters value to use in future coordination
        filters = filtersSource.getFilters()
    }

    internal fun dropFilters() {
        filters = NoFilters
    }

    internal fun notifyOnError(e: Throwable) {
        // notify all listeners to display some error UI
        listeners.forEach { it.onErrorLcDataLoading(e) }
    }

    /**
     * Refresh has been initiated from [source] listener, so one can notify all other listeners.
     * If [source] is 'null', all listeners will be notified.
     */
    internal fun notifyOnRefresh(source: LcDataListener?) {
        // notify all listeners except 'source' to display some refreshing UI
        listeners.forEach { if (it != source) it.onStartLcDataLoading() }
    }

    @Suppress("CheckResult")
    private fun refreshIfUserHasImages() {
        countUserImagesUseCase.source()
            .flatMapCompletable { countOfImages ->
                if (countOfImages > 0) {
                    // notify listeners to display some refreshing UI
                    listeners.forEach { it.onStartLcDataLoading() }
                    getLcUseCase.source(prepareGetLcParams())
                                .ignoreElement()  // convert to Completable
                } else {
                    Completable.complete()
                }
            }
            .autoDisposable(scopeProvider)
            .subscribe({}, DebugLogUtil::e)
    }

    // ------------------------------------------
    private fun prepareGetLcParams(): Params =
        Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                .put("limit", DomainUtil.LIMIT_PER_PAGE)
                // no source feed can be specified for coordinator, use some to avoid 'WrongRequestParamsClientError'
                .put("source", DomainUtil.SOURCE_FEED_LIKES)
                .put(filters)

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventAppFreshStart(event: BusEvent.AppFreshStart) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName} on ${javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get LC on Application fresh start")
        dropFilters()
        refreshIfUserHasImages()  // refresh on app fresh start
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRecreateMainScreen(event: BusEvent.RecreateMainScreen) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName} on ${javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get LC on Application recreate while running")
        dropFilters()
        refreshIfUserHasImages()  // refresh on recreate
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReOpenAppOnPush(event: BusEvent.ReOpenAppOnPush) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName} on ${javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get LC on Application reopen")
        dropFilters()
        refreshIfUserHasImages()  // app reopen leads LC screen to refresh as well
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReStartWithTime(event: BusEvent.ReStartWithTime) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName} on ${javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("App last open was more than 5 minutes ago, refresh LC")
        dropFilters()
        refreshIfUserHasImages()  // app reopen leads LC screen to refresh as well
    }

    // note that change global filters doesn't lead LC data to reload
}
