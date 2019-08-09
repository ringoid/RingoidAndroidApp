package com.ringoid.data.repository.feed

import com.ringoid.domain.model.feed.Filters
import com.ringoid.domain.model.feed.Lmm

internal data class LcInMemory(val lmm: Lmm, val lastActionTime: Long, val filters: Filters? = null)
