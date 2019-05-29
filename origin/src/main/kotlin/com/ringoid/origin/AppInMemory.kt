package com.ringoid.origin

import com.ringoid.domain.misc.Gender

object AppInMemory {

    private var userGender: Gender = Gender.UNKNOWN

    internal fun setUserGender(gender: Gender) {
        userGender = gender
    }

    fun userGender(): Gender = userGender

    fun oppositeUserGender(): Gender =
        when (userGender) {
            Gender.MALE -> Gender.FEMALE
            Gender.FEMALE -> Gender.MALE
            else -> Gender.UNKNOWN
        }
}
