package com.ringoid.data.action_storage

import java.lang.RuntimeException

class WaitUntilTriggerFinishedException : RuntimeException("Waiting for commit actions in progress to finish...")
