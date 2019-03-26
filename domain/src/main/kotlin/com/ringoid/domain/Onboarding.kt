package com.ringoid.domain

enum class Onboarding {
    ADD_IMAGE, DIRECT;

    companion object {
        fun current(): Onboarding = valueOf(BuildConfig.ONBOARDING)
    }
}
