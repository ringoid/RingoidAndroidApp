package com.ringoid.utility

import android.net.Uri
import android.os.Build
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

const val LOCATION_EPSf = 0.000001f
const val LOCATION_EPS = 0.000001

fun Uri.extension(): String =
    toString().takeIf { it.contains('.') }
              ?.let { it.substring(it.lastIndexOf('.') + 1) } ?: ""

fun Uri.paths(): List<String> = toString().removePrefix("$scheme://$host/").split('/')

fun priorVersion(version: Int): Boolean = Build.VERSION.SDK_INT < version
fun targetVersion(version: Int): Boolean = Build.VERSION.SDK_INT >= version

fun randomLong(): Long = UUID.randomUUID().hashCode().toLong()

fun randomString(): String = UUID.randomUUID().toString()

fun Throwable.stackTraceString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw, true)
    printStackTrace(pw)
    return sw.buffer.toString()
}
