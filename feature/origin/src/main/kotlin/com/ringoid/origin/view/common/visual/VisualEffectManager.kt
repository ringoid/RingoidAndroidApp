package com.ringoid.origin.view.common.visual

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object VisualEffectManager {

    private val effect = PublishSubject.create<VisualEffect>()
    internal fun effectSource(): Observable<VisualEffect> = effect.hide()

    fun call(effect: VisualEffect) {
        this.effect.onNext(effect)
    }
}
