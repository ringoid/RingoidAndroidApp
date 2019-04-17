package com.ringoid.data.action_storage

import java.lang.RuntimeException

class WaitUntilTriggerFinishedException(val tpayload: ProcessingPayload)
    : RuntimeException("Waiting for commit actions to finish... [t=${tpayload.threadId}], total: ${System.currentTimeMillis() - tpayload.startTime} ms")
