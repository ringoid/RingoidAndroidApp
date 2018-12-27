package com.ringoid.base.viewmodel

import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KProperty

class FragmentDelegateVmFactory {

    operator fun getValue(thisRef: Any, property: KProperty<*>): ViewModelProvider.Factory {
        //
    }
}
