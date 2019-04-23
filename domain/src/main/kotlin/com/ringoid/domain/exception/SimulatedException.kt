package com.ringoid.domain.exception

import com.ringoid.domain.debug.DebugLogUtil
import java.lang.RuntimeException

class SimulatedException : RuntimeException() {

    init {
        DebugLogUtil.e(this)
    }
}
