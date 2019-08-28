package com.ringoid.base.livedata

import androidx.lifecycle.MutableLiveData
import java.util.*

class ActiveMutableLiveData<T>(initialValue: T) : MutableLiveData<T>(initialValue) {

    private val values: Queue<T> = LinkedList()

    private var isActive: Boolean = false

    override fun onActive() {
        isActive = true
        while (values.isNotEmpty()) {
            setValue(values.poll())
        }
    }

    override fun onInactive() {
        isActive = false
    }

    override fun setValue(value: T) {
        if (isActive) {
            super.setValue(value)
        } else {
            values.add(value)
        }
    }
}
