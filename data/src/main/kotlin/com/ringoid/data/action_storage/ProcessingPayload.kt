package com.ringoid.data.action_storage

data class ProcessingPayload(val threadId: Long, val startTime: Long = System.currentTimeMillis())
