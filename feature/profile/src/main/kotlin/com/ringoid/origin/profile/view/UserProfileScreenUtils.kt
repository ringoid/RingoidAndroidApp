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

    fun hasAtLeastOneProperty(properties: UserProfileProperties): Boolean =
        properties.let {
            hasChildrenProperty(it) ||
            hasCompanyAndJobTitleProperty(it) ||
            hasEducationProperty(it) ||
            hasHairColorProperty(it) ||
            hasHeightProperty(it) ||
            hasIncomeProperty(it) ||
            hasPropertyProperty(it) ||
            hasSocialInstagramProperty(it) ||
            hasSocialTiktokProperty(it) ||
            hasTransportProperty(it) ||
            hasUniversityProperty(it) ||
            hasWhereLiveProperty(it)
        }

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
            UserProfilePropertyId.COMPANY_JOB_TITLE -> {  // custom text property
                val company = properties.company()
                val jobTitle = properties.jobTitle()
                if (company.isNotBlank() || jobTitle.isNotBlank()) {
                    val strList = mutableListOf<String>().apply {
                        jobTitle.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
                        company.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
                    }
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_company_job_white_18dp)
                        setText(strList.joinToString(", "))
                    }
                } else if (useDefault) {
                    val strList = mutableListOf<String>().apply {
                        add(container.resources.getString(OriginR_string.settings_profile_item_custom_property_job_title))
                        add(container.resources.getString(OriginR_string.settings_profile_item_custom_property_company))
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
                if (hasHeightProperty(properties)) {
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
            UserProfilePropertyId.SOCIAL_INSTAGRAM -> {  // custom text property
                val instagram = properties.instagram()
                if (instagram.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_instagram_white_18dp)
                        setText("Instagram: ${instagram.trim()}")
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_instagram_white_18dp)
                        setText(OriginR_string.settings_profile_item_custom_property_instagram)
                    }
                } else null
            }
            UserProfilePropertyId.SOCIAL_TIKTOK -> {  // custom text property
                val tiktok = properties.tiktok()
                if (tiktok.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_tiktok_white_18dp)
                        setText("TikTok: ${tiktok.trim()}")
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
            UserProfilePropertyId.UNIVERSITY -> {  // custom text property
                val university = properties.university()
                if (university.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(university.trim())
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(OriginR_string.settings_profile_item_custom_property_university)
                    }
                } else null
            }
            UserProfilePropertyId.WHERE_LIVE -> {  // custom text property
                val whereLive = properties.whereLive()
                if (whereLive.isNotBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_location_marker_white_18dp)
                        setText(whereLive.trim())
                    }
                } else if (useDefault) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_location_marker_white_18dp)
                        setText(OriginR_string.settings_profile_item_custom_property_where_live)
                    }
                } else null
            }
        }

    // --------------------------------------------------------------------------------------------
    private fun hasChildrenProperty(properties: UserProfileProperties): Boolean =
        properties.children != ChildrenProfileProperty.Unknown

    private fun hasCompanyAndJobTitleProperty(properties: UserProfileProperties): Boolean =
        properties.company().isNotBlank() || properties.jobTitle().isNotBlank()

    // distance is not defined on Profile screen

    private fun hasEducationProperty(properties: UserProfileProperties): Boolean =
        properties.education != EducationProfileProperty.Unknown

    private fun hasHairColorProperty(properties: UserProfileProperties): Boolean =
        properties.hairColor != HairColorProfileProperty.Unknown

    private fun hasHeightProperty(properties: UserProfileProperties): Boolean =
        properties.height > 0

    private fun hasIncomeProperty(properties: UserProfileProperties): Boolean =
        properties.income != IncomeProfileProperty.Unknown

    private fun hasPropertyProperty(properties: UserProfileProperties): Boolean =
        properties.property != PropertyProfileProperty.Unknown

    private fun hasSocialInstagramProperty(properties: UserProfileProperties): Boolean =
        properties.instagram().isNotBlank()

    private fun hasSocialTiktokProperty(properties: UserProfileProperties): Boolean =
        properties.tiktok().isNotBlank()

    private fun hasTransportProperty(properties: UserProfileProperties): Boolean =
        properties.transport != TransportProfileProperty.Unknown

    private fun hasUniversityProperty(properties: UserProfileProperties): Boolean =
        properties.university().isNotBlank()

    private fun hasWhereLiveProperty(properties: UserProfileProperties): Boolean =
        properties.whereLive().isNotBlank()
}
