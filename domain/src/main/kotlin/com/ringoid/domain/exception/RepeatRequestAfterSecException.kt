package com.ringoid.domain.exception

class RepeatRequestAfterSecException(val delay: Long) : RuntimeException("Repeat request after sec: $delay")
