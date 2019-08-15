package com.ringoid.base

import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.manager.IUserSettingsManager
import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.domain.scope.UserScopeProvider
import com.ringoid.utility.IAppCommunicator
import com.ringoid.utility.manager.LocaleManager
import java.util.*

interface IBaseRingoidApplication : IAppCommunicator {

    val localeManager: LocaleManager
    val imagePreviewReceiver: IImagePreviewReceiver
    val loginInMemoryCache: ILoginInMemoryCache
    val userScopeProvider: UserScopeProvider
    val userSettingsManager: IUserSettingsManager
    val updatePushTokenUseCase: UpdatePushTokenUseCase

    val calendar: Calendar
}
