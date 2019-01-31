package com.ringoid.base

import com.ringoid.domain.memory.ILoginInMemoryCache

interface IBaseRingoidApplication {

    val loginInMemoryCache: ILoginInMemoryCache
}
