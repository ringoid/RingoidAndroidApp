package com.ringoid.domain.misc

import androidx.annotation.DrawableRes
import com.ringoid.domain.R

enum class Gender(val string: String, @DrawableRes val resId: Int) {
    MALE("male", R.drawable.ic_sex_male_white_24dp),
    FEMALE("female", R.drawable.ic_sex_female_white_24dp),
    UNKNOWN("", 0);

    fun short(): Char = name.takeIf { it.isNotEmpty() }?.get(0)?.toUpperCase() ?: ' '

    companion object {
        fun from(str: String?): Gender =
            when (str) {
                "female" -> Gender.FEMALE
                "male" -> Gender.MALE
                else -> Gender.UNKNOWN
            }
    }
}
