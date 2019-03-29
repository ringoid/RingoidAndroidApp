package com.ringoid.domain.model.essence.user

import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.user.UserSettings

interface IUpdateUserSettingsEssence : IEssence {

    val userSettings: UserSettings

    override fun toSentryPayload(): String = "[locale=${userSettings.locale},push=${userSettings.push},timeZone=${userSettings.timeZone}]"
}
