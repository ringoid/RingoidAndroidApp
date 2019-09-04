package com.ringoid.report.exception

class NetworkException(val code: Int, val tag: String? = null) : RuntimeException("code=$code")
