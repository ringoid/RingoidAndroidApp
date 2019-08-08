package com.ringoid.base.eventbus

import android.os.Bundle
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import org.greenrobot.eventbus.EventBus

object Bus {

    fun post(event: Any) {
        DebugLogUtil.b("Bus: $event")
        EventBus.getDefault().post(event)
    }

    fun subscribeOnBusEvents(subscriber: Any) {
        if (!EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().register(subscriber)
        }
    }

    fun unsubscribeFromBusEvents(subscriber: Any) {
        if (EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().unregister(subscriber)
        }
    }

    fun isSubscribed(subscriber: Any): Boolean = EventBus.getDefault().isRegistered(subscriber)
}

sealed class BusEvent {

    override fun toString(): String = javaClass.simpleName

    @DebugOnly object CloseDebugView : BusEvent()
    @DebugOnly object SimulateError : BusEvent()

    object Stub : BusEvent()
    object AppFreshStart : BusEvent()
    object ReOpenApp: BusEvent()
    data class ReStartWithTime(val msElapsed: Long): BusEvent()

    object RefreshOnExplore : BusEvent()
    @Deprecated("LMM -> LC")
    data class RefreshOnLmm(val lmmSourceFeed: String) : BusEvent()
    object RefreshOnProfile : BusEvent()

    data class PushNewLike(val peerId: String) : BusEvent()
    data class PushNewMatch(val peerId: String) : BusEvent()
    data class PushNewMessage(val peerId: String) : BusEvent()

    data class TransferProfile(val profileId: String, val payload: Bundle)
}
