package com.ringoid.report.exception

class RepeatRequestAfterSecException(val delay: Long) : RuntimeException("Repeat request after delay: $delay")
