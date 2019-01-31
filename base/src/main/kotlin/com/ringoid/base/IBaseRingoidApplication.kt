package com.ringoid.base

import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.utility.IAppCommunicator

interface IBaseRingoidApplication : IAppCommunicator {

    val imagePreviewReceiver: IImagePreviewReceiver
    val loginInMemoryCache: ILoginInMemoryCache
}
