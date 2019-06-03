package com.ringoid.domain.model.essence.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "property":0,
 *   "transport":10,
 *   "income":20,
 *   "height":150,
 *   "educationLevel":10,
 *   "hairColor":0
 * }
 */
data class UpdateUserProfileEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_PROPERTY_EDUCATION) val education: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_HAIR_COLOR) val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_HEIGHT) val height: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_INCOME) val income: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_PROPERTY) val property: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_TRANSPORT) val transport: Int = DomainUtil.UNKNOWN_VALUE) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_PROPERTY_EDUCATION = "educationLevel"
        const val COLUMN_PROPERTY_HAIR_COLOR = "hairColor"
        const val COLUMN_PROPERTY_HEIGHT = "height"
        const val COLUMN_PROPERTY_INCOME = "income"
        const val COLUMN_PROPERTY_PROPERTY = "property"
        const val COLUMN_PROPERTY_TRANSPORT = "transport"

        fun from(essence: UpdateUserProfileEssenceUnauthorized, accessToken: String): UpdateUserProfileEssence =
            UpdateUserProfileEssence(
                accessToken = accessToken,
                education = essence.education,
                hairColor = essence.hairColor,
                height = essence.height,
                income = essence.income,
                property = essence.property,
                transport = essence.transport)
    }

    override fun toDebugPayload(): String = "[education=$education,hairColor=$hairColor,height=$height,income=$income,property=$property,transport=$transport]"
}
