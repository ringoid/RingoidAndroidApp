package com.ringoid.domain.scope

import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserScopeProvider @Inject constructor() : LifecycleScopeProvider<UserLifecycleEvent> {

    companion object {
        /**
         * All subscriptions will only be disposed at [UserLifecycleEvent.LOGOUT].
         */
        private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<UserLifecycleEvent> { event ->
            when (event) {
                UserLifecycleEvent.LOGIN -> UserLifecycleEvent.LOGOUT
                else -> throw LifecycleEndedException("Cannot bind to User scope lifecycle after logout.")
            }
        }
    }

    private val lifecycleEvents = BehaviorSubject.createDefault(UserLifecycleEvent.LOGIN)

    override fun lifecycle(): Observable<UserLifecycleEvent> = lifecycleEvents.hide()

    override fun correspondingEvents(): CorrespondingEventsFunction<UserLifecycleEvent> = CORRESPONDING_EVENTS

    override fun peekLifecycle(): UserLifecycleEvent? = lifecycleEvents.value

    fun onLogout() {
        Timber.d("Logout on user scope provider: ${hashCode()}")
        lifecycleEvents.onNext(UserLifecycleEvent.LOGOUT)
    }
}
