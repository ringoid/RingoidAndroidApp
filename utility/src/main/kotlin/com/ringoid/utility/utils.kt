package com.ringoid.utility

import android.net.Uri
import android.os.Build
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/* Location GPS */
// ------------------------------------------------------------------------------------------------
/**
 * For accuracy:
 *
 * @see https://gis.stackexchange.com/questions/8650/measuring-accuracy-of-latitude-and-longitude
 */
const val LOCATION_110m = 0.001
const val LOCATION_550m = 0.005
const val LOCATION_EPS = 0.000001

/* Misc */
// ------------------------------------------------------------------------------------------------
fun Uri.extension(): String =
    toString().takeIf { it.contains('.') }
              ?.let { it.substring(it.lastIndexOf('.') + 1) } ?: ""

fun Uri.paths(): List<String> = toString().removePrefix("$scheme://$host/").split('/')

fun priorVersion(version: Int): Boolean = Build.VERSION.SDK_INT < version
fun targetVersion(version: Int): Boolean = Build.VERSION.SDK_INT >= version

fun randomLong(): Long = UUID.randomUUID().hashCode().toLong()

fun randomString(): String = UUID.randomUUID().toString()
fun randomString(length: Int): String = randomString().substring(0 until length)

fun Throwable.stackTraceString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw, true)
    printStackTrace(pw)
    return sw.buffer.toString()
}

fun <K, V> Map<K, V>.ForEach(action: (key: K, value: V) -> Unit) {
    entries.forEach { action(it.key, it.value) }
}

fun <T> MutableCollection<T>.RemoveIf(predicate: (it: T) -> Boolean): Boolean {
    var removed = false
    val each = iterator()
    while (each.hasNext()) {
        if (predicate.invoke(each.next())) {
            each.remove()
            removed = true
        }
    }
    return removed
}
