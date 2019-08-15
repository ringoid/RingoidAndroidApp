package com.ringoid.domain.model.essence.user

import com.ringoid.domain.model.user.UserSettings

/**
 * Used to construct [UpdateUserSettingsEssence] later assigning access token retrieved from data layer.
 */
data class UpdateUserSettingsEssenceUnauthorized(override val userSettings: UserSettings) : IUpdateUserSettingsEssence
