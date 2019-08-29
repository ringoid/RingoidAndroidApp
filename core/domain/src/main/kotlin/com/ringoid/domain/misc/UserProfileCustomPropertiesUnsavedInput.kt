package com.ringoid.domain.misc

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

data class UserProfileCustomPropertiesUnsavedInput(
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_ABOUT) var about: String = "")
    : IEssence {

    companion object {
        const val COLUMN_PROPERTY_CUSTOM_ABOUT = "about"
    }
}
