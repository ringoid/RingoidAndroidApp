package com.ringoid.domain.model.debug

import com.ringoid.domain.debug.DebugOnly
import com.ringoid.utility.randomLong

const val DEFAULT_SIZE = 200

@DebugOnly
sealed class DebugBlob(size: Int = DEFAULT_SIZE, val tag: String) {

    private val array = Array(size * size) { randomLong() }

    override fun toString(): String = tag
}

@DebugOnly
class HorizontalDebugBlob(size: Int = DEFAULT_SIZE, tag: String) : DebugBlob(size, tag)

@DebugOnly
class VerticalDebugBlob(size: Int = DEFAULT_SIZE, tag: String) : DebugBlob(size, tag)
