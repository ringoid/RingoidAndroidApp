package com.ringoid.domain.manager

import com.ringoid.domain.model.user.UserSettings

interface IUserSettingsManager {

    fun getUserSettings(): UserSettings
}
