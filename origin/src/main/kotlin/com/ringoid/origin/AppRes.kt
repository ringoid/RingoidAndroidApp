package com.ringoid.origin

import android.content.Context

object AppRes {

    var BLOCK_BOTTOM_SHEET_DIALOG_HEIGHT: Int = -1
        private set

    // ------------------------------------------
    var EMAIL_OFFICER_MAIL_SUBJECT = ""
        private set
    var EMAIL_SUPPORT_MAIL_SUBJECT = ""
        private set

    var WEB_URL_ERROR_STATUS: String = ""
        private set
    var WEB_URL_LICENSES: String = ""
        private set
    var WEB_URL_PRIVACY: String = ""
        private set
    var WEB_URL_TERMS: String = ""
        private set

    fun init(context: Context) {
        context.resources.apply {
            BLOCK_BOTTOM_SHEET_DIALOG_HEIGHT = getDimensionPixelSize(R.dimen.dialog_bottom_sheet_block_height)

            // ----------------------------------
            EMAIL_OFFICER_MAIL_SUBJECT = getString(R.string.settings_info_email_officer_mail_subject)
            EMAIL_SUPPORT_MAIL_SUBJECT = getString(R.string.settings_support_mail_subject)

            WEB_URL_ERROR_STATUS = getString(R.string.web_url_error_status)
            WEB_URL_LICENSES = getString(R.string.web_url_licenses)
            WEB_URL_PRIVACY = getString(R.string.web_url_privacy)
            WEB_URL_TERMS = getString(R.string.web_url_terms)
        }
    }
}
