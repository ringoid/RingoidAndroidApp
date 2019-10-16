package com.ringoid.utility

import android.net.Uri
import android.os.Build
import android.os.Bundle
import org.greenrobot.essentials.hash.Murmur3F
import org.json.JSONObject
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.regex.Pattern

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

val murmur3F by lazy { Murmur3F() }

/* Misc */
// ------------------------------------------------------------------------------------------------
fun Boolean.asInt(): Int = takeIf { this }?.let { 1 } ?: 0

fun checkForNull(state: Bundle?): String? = state?.let { "saved state" }

inline fun <reified T> Collection<T>.cloneAsList(): List<T> = ArrayList(this.map { it })

fun Uri.extension(): String =
    toString().takeIf { it.contains('.') }
              ?.let { it.substring(it.lastIndexOf('.') + 1) } ?: ""

fun Uri.paths(): List<String> = toString().removePrefix("$scheme://$host/").split('/')

fun priorVersion(version: Int): Boolean = Build.VERSION.SDK_INT < version
fun targetVersion(version: Int): Boolean = Build.VERSION.SDK_INT >= version

fun notBlankOf(lhs: String?, rhs: String?): String =
    lhs.takeUnless { it.isNullOrBlank() }
        ?: rhs.takeUnless { it.isNullOrBlank() }
        ?: ""

fun randomInt(): Int = UUID.randomUUID().hashCode()
fun randomLong(): Long = randomInt().toLong()

fun randomString(): String = UUID.randomUUID().toString()
fun randomString(length: Int): String = randomString().substring(0 until length)

fun String.goodHashCode(): Long = murmur3F.let { it.update(toByteArray(Charsets.UTF_8)); it.value }
fun String.upToNChar(n: Int): String = substring(0, minOf(length, n))

fun Throwable.stackTraceString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw, true)
    printStackTrace(pw)
    return sw.buffer.toString()
}

fun Throwable.stackTraceStringN(n: Int): String = stackTraceString().upToNChar(n)

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

/* Json */
// ------------------------------------------------------------------------------------------------
fun String.extractJsonProperty(key: String): String? =
    JSONObject(trim().replace('"', '\"')).optString(key)

/* Log utils */
// ------------------------------------------------------------------------------------------------
private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
const val MAX_TAG_LENGTH = 23
const val CALL_STACK_INDEX = 5

fun tagLine(prefix: String = "") {
    Throwable().stackTrace.getOrNull(CALL_STACK_INDEX)?.className
        ?.let { tag ->
            val m = ANONYMOUS_CLASS.matcher(tag)
            if (m.find()) m.replaceAll("") else tag
        }
        ?.let { it.substring(it.lastIndexOf('.') + 1) }
        ?.let { tag ->
            if (tag.length <= MAX_TAG_LENGTH || targetVersion(Build.VERSION_CODES.N)) tag
            else tag.substring(0, MAX_TAG_LENGTH)
        }
        ?.also { tag -> Timber.tag("$prefix$tag") }
}

object SysTimber {

    fun d(msg: String) { println(msg) }
    fun d(e: Throwable, msg: String) { println("${e.javaClass.simpleName}: ${e.message} $msg") }
    fun i(msg: String) { println(msg) }
    fun i(e: Throwable, msg: String) { println("${e.javaClass.simpleName}: ${e.message} $msg") }
    fun e(e: Throwable) { println("${e.javaClass.simpleName}: ${e.message}") }
    fun v(msg: String) { println(msg) }
    fun v(e: Throwable, msg: String) { println("${e.javaClass.simpleName}: ${e.message} $msg") }
}
