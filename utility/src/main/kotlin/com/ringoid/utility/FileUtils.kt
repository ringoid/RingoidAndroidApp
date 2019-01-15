package com.ringoid.utility

import android.net.Uri
import java.io.File

fun File.uri(): Uri = Uri.parse(toURI().toString())

fun File.uriString(): String = uri().toString()
