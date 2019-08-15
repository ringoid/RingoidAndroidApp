package com.ringoid.origin.view.common.visual

import io.reactivex.subjects.PublishSubject

object VisualEffectManager {

    internal val effect = PublishSubject.create<VisualEffect>()

    fun call(effect: VisualEffect) {
        this.effect.onNext(effect)
    }
}
