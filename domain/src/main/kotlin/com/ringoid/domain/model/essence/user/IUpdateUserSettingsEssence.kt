package com.ringoid.domain.model.essence.user

import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.user.UserSettings

interface IUpdateUserSettingsEssence : IEssence {

    val userSettings: UserSettings

    override fun toSentryPayload(): String = "[locale=${userSettings.locale},push=${userSettings.push}," +
            "pushLikes=${userSettings.pushLikes},pushMatches=${userSettings.pushMatches}" +
            "pushMessages=${userSettings.pushMessages},timeZone=${userSettings.timeZone}]"
}
