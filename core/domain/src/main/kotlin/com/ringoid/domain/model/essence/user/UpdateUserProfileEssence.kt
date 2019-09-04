package com.ringoid.domain.model.essence.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "children":10,
 *   "property":0,
 *   "transport":10,
 *   "income":20,
 *   "height":150,
 *   "educationLevel":10,
 *   "hairColor":0,
 *
 *   "name":"Mikhail",
 *   "jobTitle":"Developer",
 *   "company":"Ringoid",
 *   "education":"BGTU Voenmeh",
 *   "about":"Nice person",
 *   "instagram":"unknown",
 *   "tikTok":"unknown",
 *   "whereLive":"St.Petersburg",
 *   "whereFrom":"Leningrad"
 * }
 */
data class UpdateUserProfileEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_PROPERTY_CHILDREN) val children: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_EDUCATION) val education: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_HAIR_COLOR) val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_HEIGHT) val height: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_INCOME) val income: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_PROPERTY) val property: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_TRANSPORT) val transport: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_ABOUT) val about: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_COMPANY) val company: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_JOB_TITLE) val jobTitle: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_NAME) val name: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_SOCIAL_INSTAGRAM) val instagram: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_SOCIAL_TIKTOK) val tiktok: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_STATUS_TEXT) var statusText: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_UNIVERSITY) val university: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_WHERE_FROM) val whereFrom: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_WHERE_LIVE) val whereLive: String = "") : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_PROPERTY_CHILDREN = "children"
        const val COLUMN_PROPERTY_EDUCATION = "educationLevel"
        const val COLUMN_PROPERTY_HAIR_COLOR = "hairColor"
        const val COLUMN_PROPERTY_HEIGHT = "height"
        const val COLUMN_PROPERTY_INCOME = "income"
        const val COLUMN_PROPERTY_PROPERTY = "property"
        const val COLUMN_PROPERTY_TRANSPORT = "transport"
        const val COLUMN_PROPERTY_CUSTOM_ABOUT = "about"
        const val COLUMN_PROPERTY_CUSTOM_COMPANY = "company"
        const val COLUMN_PROPERTY_CUSTOM_JOB_TITLE = "jobTitle"
        const val COLUMN_PROPERTY_CUSTOM_NAME = "name"
        const val COLUMN_PROPERTY_CUSTOM_SOCIAL_INSTAGRAM = "instagram"
        const val COLUMN_PROPERTY_CUSTOM_SOCIAL_TIKTOK = "tikTok"
        const val COLUMN_PROPERTY_CUSTOM_STATUS_TEXT = "statusText"
        const val COLUMN_PROPERTY_CUSTOM_UNIVERSITY = "education"
        const val COLUMN_PROPERTY_CUSTOM_WHERE_FROM = "whereFrom"
        const val COLUMN_PROPERTY_CUSTOM_WHERE_LIVE = "whereLive"

        fun from(essence: UpdateUserProfileEssenceUnauthorized, accessToken: String): UpdateUserProfileEssence =
            UpdateUserProfileEssence(
                accessToken = accessToken,
                children = essence.children,
                education = essence.education,
                hairColor = essence.hairColor,
                height = essence.height,
                income = essence.income,
                property = essence.property,
                transport = essence.transport,
                about = essence.about,
                company = essence.company,
                jobTitle = essence.jobTitle,
                name = essence.name,
                statusText = essence.statusText,
                instagram = essence.socialInstagram,
                tiktok = essence.socialTikTok,
                university = essence.university,
                whereFrom = essence.whereFrom,
                whereLive = essence.whereLive)
    }

    override fun toDebugPayload(): String = "[children=$children,education=$education,hairColor=$hairColor,height=$height,income=$income,property=$property,transport=$transport,about=$about,company=$company,jobTitle=$jobTitle,name=$name,statusText=$statusText,instagram=$instagram,tiktok=$tiktok,university=$university,whereFrom=$whereFrom,whereLive=$whereLive]"
}
