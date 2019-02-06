package com.ringoid.base

import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.domain.scope.UserScopeProvider
import com.ringoid.utility.IAppCommunicator

interface IBaseRingoidApplication : IAppCommunicator {

    val imagePreviewReceiver: IImagePreviewReceiver
    val loginInMemoryCache: ILoginInMemoryCache
    val userScopeProvider: UserScopeProvider
}
