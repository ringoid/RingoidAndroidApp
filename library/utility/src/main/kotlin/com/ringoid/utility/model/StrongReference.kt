package com.ringoid.utility.model

import java.lang.ref.WeakReference

/**
 * Wrapper for a strong reference of any type to simplify replacement with [WeakReference] where need.
 */
data class StrongReference<T>(private val referent: T) {

    fun get(): T? = referent
}
