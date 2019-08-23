package com.ringoid.domain.model.essence.user

/**
 * Used to construct [ReferralCodeEssence] later assigning access token retrieved from data layer.
 */
data class ReferralCodeEssenceUnauthorized(override val referralId: String) : IReferralCodeEssence
