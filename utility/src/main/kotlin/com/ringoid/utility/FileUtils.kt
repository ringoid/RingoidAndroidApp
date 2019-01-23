package com.ringoid.utility

import android.net.Uri
import io.reactivex.Observable
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

fun File.uri(): Uri = Uri.parse(toURI().toString())

fun File.uriString(): String = uri().toString()

fun String.readFromUrl(): Observable<String> =
    Observable.create<String> { emitter ->
        try {
            val url = URL(this)
            BufferedReader(InputStreamReader(url.openStream()))
                .use { emitter.onNext(it.readLine() ?: "") }
        } catch (e: MalformedURLException) {
            Timber.e(e, "Malformed input url: $this")
            emitter.onError(e)
            return@create
        } catch (e: IOException) {
            Timber.e(e, "IO exception on url: $this")
            emitter.onError(e)
            return@create
        }
        emitter.onComplete()
    }
