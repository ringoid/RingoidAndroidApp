package com.ringoid.domain.exception

class RepeatRequestAfterSecException(delay: Int) : RuntimeException("Repeat request after sec: $delay")
