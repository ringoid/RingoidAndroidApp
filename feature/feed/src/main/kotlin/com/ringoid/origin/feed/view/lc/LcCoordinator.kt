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
        fun onStartLcDataLoading()
    }

    private var filters: Filters = NoFilters
    private val listeners: MutableList<LcDataListener> = mutableListOf()

    internal fun init() {
        Bus.subscribeOnBusEvents(subscriber = this)
    }

    internal fun deinit() {
        if (Bus.isSubscribed(subscriber = this)) {
            Bus.unsubscribeFromBusEvents(subscriber = this)
        }
    }

    // ------------------------------------------
    internal fun registerListener(l: LcDataListener) {
        listeners.add(l)
    }

    internal fun unregisterListener(l: LcDataListener) {
        listeners.remove(l)
    }

    // ------------------------------------------
    @Suppress("CheckResult")
    private fun refreshIfUserHasImages() {
        countUserImagesUseCase.source()
            // TODO: autoDispose
            .flatMapCompletable { countOfImages ->
                if (countOfImages > 0) {
                    listeners.forEach { it.onStartLcDataLoading() }  // notify listeners to display some refreshing UI
                    getLcUseCase.source(prepareGetLcParams())
                                .ignoreElement()  // convert to Completable
                } else {
                    Completable.complete()
                }
            }
            .subscribe({}, DebugLogUtil::e)
    }

    internal fun onApplyFilters() {
        // update filters value to use in future coordination
        filters = filtersSource.getFilters()
    }

    internal fun dropFilters() {
        filters = NoFilters
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
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get LC on Application fresh start")
        dropFilters()
        refreshIfUserHasImages()  // refresh on app fresh start
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRecreateMainScreen(event: BusEvent.RecreateMainScreen) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get LC on Application recreate while running")
        dropFilters()
        refreshIfUserHasImages()  // refresh on recreate
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReOpenApp(event: BusEvent.ReOpenAppOnPush) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        DebugLogUtil.i("Get LC on Application reopen")
        dropFilters()
        refreshIfUserHasImages()  // app reopen leads LC screen to refresh as well
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReStartWithTime(event: BusEvent.ReStartWithTime) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        if (event.msElapsed in 300000L..1557989300340L) {
            DebugLogUtil.i("App last open was more than 5 minutes ago, refresh LC")
            dropFilters()
            refreshIfUserHasImages()  // app reopen leads LC screen to refresh as well
        }
    }
}
