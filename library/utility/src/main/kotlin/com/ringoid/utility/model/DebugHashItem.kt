package com.ringoid.utility.model

import com.ringoid.utility.DebugOnly
import com.ringoid.utility.goodHashCode

@DebugOnly
class DebugHashItem(val id: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DebugHashItem) return false

        /**
         * Hash code is used to compare instances. This allows to detect outside
         * whether two different instances have the same hash code, so they cannot
         * be added to the same Set that disallows duplicates.
         */
        if (id.hashCode() != other.id.hashCode()) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

    fun goodHashCode(): Long = id.goodHashCode()

    override fun toString(): String = "[ id=$id h=${hashCode()} g=${goodHashCode()} ]"
}
