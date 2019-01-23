package com.ringoid.utility

import android.net.Uri
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

fun File.uri(): Uri = Uri.parse(toURI().toString())

fun File.uriString(): String = uri().toString()

fun String.readFromUrl(): String? {
    var line: String? = null
    try {
        val url = URL(this)
        BufferedReader(InputStreamReader(url.openStream()))
            .use { line = it.readLine() }
    } catch (e: MalformedURLException) {
        Timber.e(e, "Malformed input url: $this")
    } catch (e: IOException) {
        Timber.e(e, "IO exception on url: $this")
    }
    return line
}
