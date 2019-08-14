package com.ringoid.base.navigation

enum class AppScreen(val idempotent: Boolean = true) {
    STUB(idempotent = false),
    UNKNOWN(idempotent = false),
    IMAGE_PREVIEW,
    LOGIN,
    MAIN,
    SETTINGS,
    SETTINGS_DEBUG,
    SETTINGS_FILTERS,
    SETTINGS_INFO,
    SETTINGS_LANGUAGE,
    SETTINGS_PROFILE,
    SETTINGS_PUSH
}
