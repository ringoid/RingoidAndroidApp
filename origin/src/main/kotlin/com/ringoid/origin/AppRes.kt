package com.ringoid.origin

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

object AppRes {

    var BUTTON_HEIGHT: Int = -1
        private set
    var BUTTON_FLAT_TEXT_SIZE: Int = -1
        private set
    var BUTTON_FLAT_INC_TEXT_SIZE: Int = -1
        private set
    var BLOCK_BOTTOM_SHEET_DIALOG_HEIGHT: Int = -1
        private set
    var ICON_SIZE_36: Int = -1
        private set
    var FEED_ITEM_FOOTER_LABEL_BOTTOM: Int = -1
        private set
    var FEED_IMAGE_HEIGHT: Int = -1
        private set
    var FEED_ITEM_BIAS_BTN_BOTTOM: Int = -1
        private set
    var FEED_ITEM_BIAS_BTN_TOP: Int = -1
        private set
    var FEED_ITEM_MID_BTN_BOTTOM: Int = -1
        private set
    var FEED_ITEM_MID_BTN_TOP: Int = -1
        private set
    var FEED_ITEM_SETTINGS_BTN_BOTTOM: Int = -1
        private set
    var FEED_ITEM_SETTINGS_BTN_TOP: Int = -1
        private set
    var FEED_ITEM_TABS_INDICATOR_BOTTOM: Int = -1
        private set
    var FEED_ITEM_TABS_INDICATOR_TOP: Int = -1
        private set
    var LMM_TOP_TAB_BAR_HIDE_AREA_HEIGHT: Int = -1
        private set
    var MAIN_BOTTOM_BAR_HEIGHT: Int = -1
        private set

    // ------------------------------------------
    lateinit var UNDERSCORE_DRAWABLE: Drawable

    // ------------------------------------------
    var EMAIL_OFFICER_MAIL_SUBJECT = ""
        private set
    var EMAIL_SUPPORT_MAIL_SUBJECT = ""
        private set

    var REPORT_DESCRIPTION = ""
        private set

    var WEB_URL_LICENSES: String = ""
        private set
    var WEB_URL_PRIVACY: String = ""
        private set
    var WEB_URL_TERMS: String = ""
        private set

    fun init(context: Context) {
        context.resources.apply {
            BUTTON_HEIGHT = getDimensionPixelSize(R.dimen.std_btn_height)
            BUTTON_FLAT_TEXT_SIZE = getDimensionPixelSize(R.dimen.std_text_18)
            BUTTON_FLAT_INC_TEXT_SIZE = getDimensionPixelSize(R.dimen.std_text_20)
            BLOCK_BOTTOM_SHEET_DIALOG_HEIGHT = getDimensionPixelSize(R.dimen.dialog_bottom_sheet_block_height)
            ICON_SIZE_36 = getDimensionPixelSize(R.dimen.std_icon_36)
            FEED_ITEM_FOOTER_LABEL_BOTTOM = getDimensionPixelSize(R.dimen.std_margin_16)
            FEED_IMAGE_HEIGHT = getDimensionPixelSize(R.dimen.std_image_height)
            FEED_ITEM_BIAS_BTN_BOTTOM = (FEED_IMAGE_HEIGHT * 0.34f).toInt()
            FEED_ITEM_BIAS_BTN_TOP = (FEED_ITEM_BIAS_BTN_BOTTOM - ICON_SIZE_36 * 0.6f).toInt()
            FEED_ITEM_MID_BTN_BOTTOM = ((FEED_IMAGE_HEIGHT + ICON_SIZE_36) * 0.5f).toInt()
            FEED_ITEM_MID_BTN_TOP = (FEED_ITEM_MID_BTN_BOTTOM - ICON_SIZE_36 * 0.8f).toInt()
            FEED_ITEM_TABS_INDICATOR_TOP = getDimensionPixelSize(R.dimen.std_margin_16)
            FEED_ITEM_TABS_INDICATOR_BOTTOM = (FEED_ITEM_TABS_INDICATOR_TOP * 1.5f).toInt()
            FEED_ITEM_SETTINGS_BTN_TOP = FEED_ITEM_TABS_INDICATOR_TOP  // getDimensionPixelSize(R.dimen.std_margin_24)
            FEED_ITEM_SETTINGS_BTN_BOTTOM = (FEED_ITEM_SETTINGS_BTN_TOP * 1.5f).toInt()
            LMM_TOP_TAB_BAR_HIDE_AREA_HEIGHT = (BUTTON_HEIGHT * 0.67f).toInt()
            MAIN_BOTTOM_BAR_HEIGHT = getDimensionPixelSize(R.dimen.main_bottom_bar_height)

            // ----------------------------------
            UNDERSCORE_DRAWABLE = ContextCompat.getDrawable(context, WidgetR_drawable.underscore)!!

            // ----------------------------------
            EMAIL_OFFICER_MAIL_SUBJECT = getString(R.string.settings_info_email_officer_mail_subject)
            EMAIL_SUPPORT_MAIL_SUBJECT = getString(R.string.settings_support_mail_subject)

            REPORT_DESCRIPTION = getString(R.string.report_profile_dialog_description)

            WEB_URL_LICENSES = getString(R.string.web_url_licenses)
            WEB_URL_PRIVACY = getString(R.string.web_url_privacy)
            WEB_URL_TERMS = getString(R.string.web_url_terms)
        }
    }
}
