package com.ringoid.utility

import android.net.Uri
import java.util.*

fun Uri.extension(): String =
    toString().takeIf { it.contains('.') }
              ?.let { it.substring(it.lastIndexOf('.') + 1) } ?: ""

fun randomLong(): Long = UUID.randomUUID().hashCode().toLong()

fun randomString(): String = UUID.randomUUID().toString()
