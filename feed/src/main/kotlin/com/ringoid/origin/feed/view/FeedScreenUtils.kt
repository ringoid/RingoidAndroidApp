package com.ringoid.origin.feed.view

import android.view.ViewGroup
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.misc.UserProfilePropertyId
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_drawable
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.model.*
import com.ringoid.widget.view.LabelView

internal object FeedScreenUtils {

    const val COUNT_LABELS_ON_PAGE = 2

    val propertiesMale =
        listOf(UserProfilePropertyId.CHILDREN,
               UserProfilePropertyId.INCOME,
               UserProfilePropertyId.COMPANY_JOB_TITLE,
               UserProfilePropertyId.UNIVERSITY,
               UserProfilePropertyId.EDUCATION_LEVEL,
               UserProfilePropertyId.PROPERTY,
               UserProfilePropertyId.TRANSPORT,
               UserProfilePropertyId.SOCIAL_TIKTOK,
               UserProfilePropertyId.SOCIAL_INSTAGRAM)

    val propertiesFemale =
        listOf(UserProfilePropertyId.CHILDREN,
               UserProfilePropertyId.UNIVERSITY,
               UserProfilePropertyId.EDUCATION_LEVEL,
               UserProfilePropertyId.INCOME,
               UserProfilePropertyId.PROPERTY,
               UserProfilePropertyId.COMPANY_JOB_TITLE,
               UserProfilePropertyId.TRANSPORT,
               UserProfilePropertyId.SOCIAL_TIKTOK,
               UserProfilePropertyId.SOCIAL_INSTAGRAM)

    val propertiesRight =
        listOf(UserProfilePropertyId.WHERE_LIVE,
               UserProfilePropertyId.DISTANCE,
               UserProfilePropertyId.HEIGHT,
               UserProfilePropertyId.HAIR_COLOR)

    fun createLabelView(
            container: ViewGroup, gender: Gender,
            propertyId: UserProfilePropertyId,
            properties: FeedItemVO): LabelView? =
        when (propertyId) {
            UserProfilePropertyId.CHILDREN -> {
                val children = properties.children()
                if (children != ChildrenProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_children_white_18dp)
                        setText(children.resId)
                    }
                } else null
            }
            UserProfilePropertyId.COMPANY_JOB_TITLE -> {  // custom text property
                val company = properties.company()
                val jobTitle = properties.jobTitle()
                if (!company.isNullOrBlank() || !jobTitle.isNullOrBlank()) {
                    val strList = mutableListOf<String>().apply {
                        jobTitle.takeIf { !it.isNullOrBlank() }?.let { add(it.trim()) }
                        company.takeIf { !it.isNullOrBlank() }?.let { add(it.trim()) }
                    }
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_company_job_white_18dp)
                        setText(strList.joinToString(", "))
                    }
                } else null
            }
            UserProfilePropertyId.DISTANCE -> {
                if (!properties.distanceText.isNullOrBlank() &&
                    properties.distanceText != DomainUtil.BAD_PROPERTY) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_location_arrow_white_18dp)
                        setText(properties.distanceText)
                    }
                } else null
            }
            UserProfilePropertyId.EDUCATION_LEVEL -> {
                val education = properties.education()
                if (education != EducationProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(education.resId)
                    }
                } else null
            }
            UserProfilePropertyId.HAIR_COLOR -> {
                val hairColor = properties.hairColor()
                if (hairColor != HairColorProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_hair_color_white_18dp)
                        setText(hairColor.resId(gender))
                    }
                } else null
            }
            UserProfilePropertyId.HEIGHT -> {
                if (properties.height > 0) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_height_property_white_18dp)
                        setText("${properties.height} ${AppRes.LENGTH_CM}")
                    }
                } else null
            }
            UserProfilePropertyId.INCOME -> {
                val income = properties.income()
                if (income != IncomeProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_income_white_18dp)
                        setText(income.resId)
                    }
                } else null
            }
            UserProfilePropertyId.PROPERTY -> {
                val property = properties.property()
                if (property != PropertyProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_home_property_white_18dp)
                        setText(property.resId)
                    }
                } else null
            }
            UserProfilePropertyId.SOCIAL_INSTAGRAM -> {  // custom text property
                val instagram = properties.instagram()
                if (!instagram.isNullOrBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_instagram_white_18dp)
                        setText("Instagram: ${instagram.trim()}")
                    }
                } else null
            }
            UserProfilePropertyId.SOCIAL_TIKTOK -> {  // custom text property
                val tiktok = properties.tiktok()
                if (!tiktok.isNullOrBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_tiktok_white_18dp)
                        setText("TikTok: ${tiktok.trim()}")
                    }
                } else null
            }
            UserProfilePropertyId.TRANSPORT -> {
                val transport = properties.transport()
                if (transport != TransportProfileProperty.Unknown) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_transport_white_18dp)
                        setText(transport.resId)
                    }
                } else null
            }
            UserProfilePropertyId.UNIVERSITY -> {  // custom text property
                val university = properties.university()
                if (!university.isNullOrBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_education_white_18dp)
                        setText(university.trim())
                    }
                } else null
            }
            UserProfilePropertyId.WHERE_LIVE -> {  // custom text property
                val whereLive = properties.whereLive()
                if (!whereLive.isNullOrBlank()) {
                    LabelView(container.context).apply {
                        setIcon(OriginR_drawable.ic_location_marker_white_18dp)
                        setText(whereLive.trim())
                    }
                } else null
            }
        }
}
