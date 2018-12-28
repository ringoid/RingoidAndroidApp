package com.ringoid.base.viewmodel

import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KProperty

class ActivityDelegateVmFactory {

    operator fun getValue(thisRef: Any, property: KProperty<*>): ViewModelProvider.Factory {
        // TODO: use dagger binds
        return DaggerViewModelFactory<>()
    }
}
