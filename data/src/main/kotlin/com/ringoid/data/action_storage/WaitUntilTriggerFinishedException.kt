package com.ringoid.data.action_storage

import java.lang.RuntimeException

class WaitUntilTriggerFinishedException(val tid: Long = -1L) : RuntimeException("Waiting for commit actions in progress to finish... [$tid]")
