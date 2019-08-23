package com.ringoid.domain.misc

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

data class UserProfileCustomPropertiesRaw(
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_ABOUT) var about: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_COMPANY) var company: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_JOB_TITLE) var jobTitle: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_HEIGHT) var height: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_NAME) var name: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_SOCIAL_INSTAGRAM) var instagram: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_SOCIAL_TIKTOK) var tiktok: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_UNIVERSITY) var university: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_WHERE_FROM) var whereFrom: String = "",
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_WHERE_LIVE) var whereLive: String = "")
    : IEssence {

    companion object {
        const val COLUMN_PROPERTY_CUSTOM_ABOUT = "about"
        const val COLUMN_PROPERTY_CUSTOM_COMPANY = "company"
        const val COLUMN_PROPERTY_CUSTOM_JOB_TITLE = "jobTitle"
        const val COLUMN_PROPERTY_CUSTOM_HEIGHT = "height"
        const val COLUMN_PROPERTY_CUSTOM_NAME = "name"
        const val COLUMN_PROPERTY_CUSTOM_SOCIAL_INSTAGRAM = "instagram"
        const val COLUMN_PROPERTY_CUSTOM_SOCIAL_TIKTOK = "tikTok"
        const val COLUMN_PROPERTY_CUSTOM_UNIVERSITY = "education"
        const val COLUMN_PROPERTY_CUSTOM_WHERE_FROM = "whereFrom"
        const val COLUMN_PROPERTY_CUSTOM_WHERE_LIVE = "whereLive"
    }
}
