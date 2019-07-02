package com.ringoid.origin.profile.view

import android.view.ViewGroup
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.misc.UserProfilePropertyId
import com.ringoid.origin.AppRes
import com.ringoid.origin.model.*
import com.ringoid.origin.profile.OriginR_drawable
import com.ringoid.origin.profile.OriginR_string
import com.ringoid.widget.view.LabelView

internal object UserProfileScreenUtils {

    const val COUNT_LABELS_ON_PAGE = 2

    val propertiesMale =
        listOf(UserProfilePropertyId.INCOME,
               UserProfilePropertyId.COMPANY_JOB_TITLE,
               UserProfilePropertyId.CHILDREN,
               UserProfilePropertyId.PROPERTY,
               UserProfilePropertyId.TRANSPORT,
               UserProfilePropertyId.UNIVERSITY,
               UserProfilePropertyId.EDUCATION_LEVEL,
               UserProfilePropertyId.SOCIAL_TIKTOK,
               UserProfilePropertyId.SOCIAL_INSTAGRAM)

    val propertiesFemale =
        listOf(UserProfilePropertyId.SOCIAL_TIKTOK,
               UserProfilePropertyId.SOCIAL_INSTAGRAM,
               UserProfilePropertyId.CHILDREN,
               UserProfilePropertyId.EDUCATION_LEVEL,
               UserProfilePropertyId.UNIVERSITY,
               UserProfilePropertyId.COMPANY_JOB_TITLE,
               UserProfilePropertyId.INCOME,
               UserProfilePropertyId.PROPERTY,
               UserProfilePropertyId.TRANSPORT)

    val propertiesRight =
        listOf(UserProfilePropertyId.WHERE_LIVE,
               UserProfilePropertyId.DISTANCE,
               UserProfilePropertyId.HEIGHT,
               UserProfilePropertyId.HAIR_COLOR)

    fun createLabelView(
            container: ViewGroup, gender: Gender,
            propertyId: UserProfilePropertyId,
            properties: UserProfileProperties,
            useDefault: Boolean = false): LabelView? =
        when (propertyId) {
            UserProfilePropertyId.CHILDREN -> {
                if (properties.children != ChildrenProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_children_white_18dp)
                        setText(properties.children.resId)
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_children_white_18dp)
                        setText(OriginR_string.profile_property_children)
                    }
                } else null
            }
            UserProfilePropertyId.COMPANY_JOB_TITLE -> {
                if (properties.company.isNotBlank() || properties.jobTitle.isNotBlank()) {
                    val strList = mutableListOf<String>().apply {
                        properties.jobTitle.takeIf { it.isNotBlank() }?.let { add(it) }
                        properties.company.takeIf { it.isNotBlank() }?.let { add(it) }
                    }
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_company_job_white_18dp)
                        setText(strList.joinToString(", "))
                    }
                } else if (useDefault) {
                    val strList = mutableListOf<String>().apply {
                        container.resources.getString(OriginR_string.settings_profile_item_custom_property_job_title)
                        container.resources.getString(OriginR_string.settings_profile_item_custom_property_company)
                    }
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_company_job_white_18dp)
                        setText(strList.joinToString(", "))
                    }
                } else null
            }
            UserProfilePropertyId.DISTANCE -> null  // distance is not defined on Profile screen
            UserProfilePropertyId.EDUCATION_LEVEL -> {
                if (properties.education != EducationProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(properties.education.resId)
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(OriginR_string.profile_property_education)
                    }
                } else null
            }
            UserProfilePropertyId.HAIR_COLOR -> {
                if (properties.hairColor != HairColorProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_hair_color_white_18dp)
                        setText(properties.hairColor.resId(gender))
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_hair_color_white_18dp)
                        setText(OriginR_string.profile_property_hair_color)
                    }
                } else null
            }
            UserProfilePropertyId.HEIGHT -> {
                if (properties.height > 0) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_height_property_white_18dp)
                        setText("${properties.height} ${AppRes.LENGTH_CM}")
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_height_property_white_18dp)
                        setText(OriginR_string.profile_property_height)
                    }
                } else null
            }
            UserProfilePropertyId.INCOME -> {
                if (properties.income != IncomeProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_income_white_18dp)
                        setText(properties.income.resId)
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_income_white_18dp)
                        setText(OriginR_string.profile_property_income)
                    }
                } else null
            }
            UserProfilePropertyId.PROPERTY -> {
                if (properties.property != PropertyProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_home_property_white_18dp)
                        setText(properties.property.resId)
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_home_property_white_18dp)
                        setText(OriginR_string.profile_property_property)
                    }
                } else null
            }
            UserProfilePropertyId.SOCIAL_INSTAGRAM -> {
                if (properties.socialInstagram.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_instagram_white_18dp)
                        setText("Instagram: ${properties.socialInstagram}")
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_instagram_white_18dp)
                        setText(OriginR_string.settings_profile_item_custom_property_instagram)
                    }
                } else null
            }
            UserProfilePropertyId.SOCIAL_TIKTOK -> {
                if (properties.socialTikTok.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_tiktok_white_18dp)
                        setText("TikTok: ${properties.socialTikTok}")
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_tiktok_white_18dp)
                        setText(OriginR_string.settings_profile_item_custom_property_tiktok)
                    }
                } else null
            }
            UserProfilePropertyId.TRANSPORT -> {
                if (properties.transport != TransportProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_transport_white_18dp)
                        setText(properties.transport.resId)
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_transport_white_18dp)
                        setText(OriginR_string.profile_property_transport)
                    }
                } else null
            }
            UserProfilePropertyId.UNIVERSITY -> {
                if (properties.university.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(properties.university)
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(OriginR_string.settings_profile_item_custom_property_university)
                    }
                } else null
            }
            UserProfilePropertyId.WHERE_LIVE -> {
                if (properties.whereLive.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_location_marker_white_18dp)
                        setText(properties.whereLive)
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_location_marker_white_18dp)
                        setText(OriginR_string.settings_profile_item_custom_property_where_live)
                    }
                } else null
            }
        }
}
