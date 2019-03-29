package com.ringoid.utility

import android.net.Uri
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI
import java.util.*

fun Uri.extension(): String =
    toString().takeIf { it.contains('.') }
              ?.let { it.substring(it.lastIndexOf('.') + 1) } ?: ""

fun Uri.paths(): List<String> = toString().removePrefix("$scheme://$host/").split('/')

fun randomLong(): Long = UUID.randomUUID().hashCode().toLong()

fun randomString(): String = UUID.randomUUID().toString()

fun Throwable.stackTraceString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw, true)
    printStackTrace(pw)
    return sw.buffer.toString()
}
