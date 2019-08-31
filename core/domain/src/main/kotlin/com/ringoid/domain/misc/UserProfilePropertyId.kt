package com.ringoid.domain.misc

enum class UserProfilePropertyId {
    CHILDREN,
    COMPANY_JOB_TITLE,
    DISTANCE,
    EDUCATION_LEVEL,
    HAIR_COLOR,
    HEIGHT,
    INCOME,
    PROPERTY,
    SOCIAL_INSTAGRAM,
    SOCIAL_TIKTOK,
    TRANSPORT,
    UNIVERSITY,
    WHERE_LIVE;

    companion object {
        val values = values()
    }
}

enum class UserProfileEditablePropertyId {
    COMPANY,
    JOB_TITLE,
    HEIGHT,
    NAME,
    SOCIAL_INSTAGRAM,
    SOCIAL_TIKTOK,
    STATUS,
    UNIVERSITY,
    WHERE_LIVE
}