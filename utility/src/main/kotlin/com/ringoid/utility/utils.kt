package com.ringoid.utility

import android.net.Uri
import java.util.*
import kotlin.random.Random

fun Uri.extension(): String =
    toString().takeIf { it.contains('.') }
              ?.let { it.substring(it.lastIndexOf('.') + 1) } ?: ""

fun randomLong(): Long = Random.nextLong()

fun randomString(): String = UUID.randomUUID().toString()
