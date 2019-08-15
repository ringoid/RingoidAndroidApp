package com.ringoid.domain.model.essence.push

/**
 * Used to construct [PushTokenEssence] later assigning access token retrieved from data layer.
 */
data class PushTokenEssenceUnauthorized(override val pushToken: String) : IPushTokenEssence
