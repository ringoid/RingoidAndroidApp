package com.ringoid.domain.misc

enum class Gender(val string: String) {
    MALE("male"), FEMALE("female"), UNKNOWN("");

    companion object {
        fun from(str: String): Gender =
            when (str) {
                "female" -> Gender.FEMALE
                "male" -> Gender.MALE
                else -> Gender.UNKNOWN
            }
    }
}
