package com.ringoid.origin.navigation

object NavigateFrom {

    const val MAIN_TAB_FEED = "feed"
    const val MAIN_TAB_LMM = "lmm"
    const val MAIN_TAB_PROFILE = "profile"

    const val SCREEN_LOGIN = "login"
}

object Payload {

    const val PAYLOAD_FEED_NEED_REFRESH = "payload_feed_need_refresh"
    const val PAYLOAD_PROFILE_LOGIN_IMAGE_ADDED = "payload_profile_login_image_added"
    const val PAYLOAD_PROFILE_REQUEST_ADD_IMAGE = "payload_profile_request_add_image"
}

object RequestCode {

    const val RC_BLOCK_DIALOG = 11000
    const val RC_DELETE_IMAGE_DIALOG = 11001
    const val RC_SETTINGS_LANG = 11100
}
