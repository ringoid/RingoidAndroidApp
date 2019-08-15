package com.ringoid.origin.error

class UnsupportedLocaleException(langId: String) : RuntimeException("Locale [$langId] is not supported")
