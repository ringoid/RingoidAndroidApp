package com.ringoid.data.local.shared_prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StyleRes
import com.google.gson.Gson
import com.ringoid.data.manager.RuntimeConfig
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.exception.InvalidAccessTokenException
import com.ringoid.domain.exception.SilentFatalException
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.domain.misc.UserProfileCustomPropertiesUnsavedInput
import com.ringoid.domain.misc.UserProfilePropertiesRaw
import com.ringoid.domain.model.feed.EmptyFilters
import com.ringoid.domain.model.feed.Filters
import com.ringoid.domain.model.feed.NoFilters
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.utility.LOCATION_EPS
import com.ringoid.utility.randomString
import io.reactivex.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class SharedPrefsManager @Inject constructor(context: Context, private val config: RuntimeConfig)
    : ISharedPrefsManager {

    private val sharedPreferences: SharedPreferences
    private val backupSharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        backupSharedPreferences = context.getSharedPreferences(BACKUP_SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        getAppUid()  // generate app uid, if not exists
        if (isAppUpdated()) {
            Timber.d("App has been updated to ${BuildConfig.BUILD_NUMBER}")
            sharedPreferences.edit().putInt(SP_KEY_BUILD_CODE, BuildConfig.BUILD_NUMBER).apply()
            deleteLastActionTime()
        }

        DebugLogUtil.setConfig(config)
        checkAndFixFilters()
    }

    companion object {
        private const val SHARED_PREFS_FILE_NAME = "Ringoid.prefs"
        private const val BACKUP_SHARED_PREFS_FILE_NAME = "RingoidBackup.prefs"

        private const val SP_KEY_BUILD_CODE = "sp_key_build_code"
        private const val SP_KEY_APP_FIRST_LAUNCH = "sp_key_app_first_launch"
        private const val SP_KEY_APP_UID = "sp_key_app_uid"
        private const val SP_KEY_THEME = "sp_key_theme"
        @DebugOnly private const val SP_KEY_DEBUG_LOG_ENABLED = "sp_key_debug_log_enabled"
        @DebugOnly private const val SP_KEY_DEVELOPER_MODE = "sp_key_developer_mode"

        /* Auth */
        // --------------------------------------
        const val SP_KEY_AUTH_USER_ID = "sp_key_auth_user_id"
        const val SP_KEY_AUTH_USER_CREATE_TS = "sp_key_auth_user_create_ts"
        const val SP_KEY_AUTH_USER_GENDER = "sp_key_auth_user_gender"
        const val SP_KEY_AUTH_USER_YEAR_OF_BIRTH = "sp_key_auth_user_year_of_birth"
        const val SP_KEY_AUTH_ACCESS_TOKEN = "sp_key_auth_access_token"

        /* Filters */
        // --------------------------------------
        const val SP_KEY_FILTERS = "sp_key_filters"

        /* Location */
        // --------------------------------------
        const val SP_KEY_LOCATION_LATITUDE = "sp_key_location_latitude"
        const val SP_KEY_LOCATION_LONGITUDE = "sp_key_location_longitude"

        /* Actions */
        // --------------------------------------
        const val SP_KEY_LAST_ACTION_TIME = "sp_key_last_action_time"

        /* Referral program */
        // --------------------------------------
        internal const val SP_KEY_PRIVATE_KEY = "sp_key_private_key"
        internal const val SP_KEY_REFERRAL_CODE = "sp_key_referral_code"
        internal const val SP_KEY_REFERRAL_CODE_DONT_OVERRIDE = "sp_key_referral_code_dont_override"

        /* User Settings */
        // --------------------------------------
        const val SP_KEY_USER_SETTINGS_DAILY_PUSH_ENABLED = "sp_key_user_settings_daily_push_enabled"
        const val SP_KEY_USER_SETTINGS_LIKES_PUSH_ENABLED = "sp_key_user_settings_likes_push_enabled"
        const val SP_KEY_USER_SETTINGS_MATCHES_PUSH_ENABLED = "sp_key_user_settings_matches_push_enabled"
        const val SP_KEY_USER_SETTINGS_MESSAGES_PUSH_ENABLED = "sp_key_user_settings_messages_push_enabled"
        const val SP_KEY_USER_SETTINGS_VIBRATION_PUSH_ENABLED = "sp_key_user_settings_vibration_push_enabled"

        const val SP_KEY_USER_PROFILE_PROPERTY_CHILDREN = "sp_key_user_profile_property_children"
        const val SP_KEY_USER_PROFILE_PROPERTY_EDUCATION  = "sp_key_user_profile_property_education"
        const val SP_KEY_USER_PROFILE_PROPERTY_HAIR_COLOR = "sp_key_user_profile_property_hair_color"
        const val SP_KEY_USER_PROFILE_PROPERTY_HEIGHT = "sp_key_user_profile_property_height"
        const val SP_KEY_USER_PROFILE_PROPERTY_INCOME = "sp_key_user_profile_property_income"
        const val SP_KEY_USER_PROFILE_PROPERTY_PROPERTY = "sp_key_user_profile_property_property"
        const val SP_KEY_USER_PROFILE_PROPERTY_TRANSPORT = "sp_key_user_profile_property_transport"

        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_ABOUT = "sp_key_user_profile_custom_property_about"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_COMPANY = "sp_key_user_profile_custom_property_company"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_JOB_TITLE = "sp_key_user_profile_custom_property_job_title"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_NAME = "sp_key_user_profile_custom_property_name"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_STATUS_TEXT = "sp_key_user_profile_custom_property_status_text"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_INSTAGRAM = "sp_key_user_profile_custom_property_social_instagram"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_TIKTOK = "sp_key_user_profile_custom_property_social_tiktok"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_UNIVERSITY = "sp_key_user_profile_custom_property_university"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_FROM = "sp_key_user_profile_custom_property_where_from"
        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_LIVE = "sp_key_user_profile_custom_property_where_live"

        const val SP_KEY_USER_PROFILE_CUSTOM_PROPERTIES_UNSAVED_INPUT = "sp_key_user_profile_custom_properties_unsaved_input"

        /* Misc */
        // --------------------------------------
        const val SP_KEY_BIG_EDIT_TEXT = "sp_key_big_edit_text"
        const val SP_KEY_FLAG_NEED_SHOW_FILTERS = "sp_key_flag_need_show_filters"
    }

    // --------------------------------------------------------------------------------------------
    override fun getAppUid(): String =
        sharedPreferences.getString(SP_KEY_APP_UID, null)
            ?: run { randomString().also { sharedPreferences.edit().putString(SP_KEY_APP_UID, it).apply() } }

    override fun isFirstAppLaunch(): Boolean = sharedPreferences.getBoolean(SP_KEY_APP_FIRST_LAUNCH, true)

    override fun dropFirstAppLaunch() {
        sharedPreferences.edit().putBoolean(SP_KEY_APP_FIRST_LAUNCH, false).apply()
    }

    private fun isAppUpdated(): Boolean =
        sharedPreferences.getInt(SP_KEY_BUILD_CODE, 0) < BuildConfig.BUILD_NUMBER

    // ------------------------------------------
    override fun getByKey(key: String): String? = sharedPreferences.getString(key, null)

    override fun saveByKey(key: String, json: String) {
        sharedPreferences.edit().putString(key, json).apply()
    }

    override fun deleteByKey(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // ------------------------------------------
    @StyleRes
    override fun getThemeResId(@StyleRes defaultThemeResId: Int): Int = sharedPreferences.getInt(SP_KEY_THEME, defaultThemeResId)

    override fun saveThemeResId(@StyleRes themeResId: Int) {
        sharedPreferences.edit().putInt(SP_KEY_THEME, themeResId).apply()
    }

    // ------------------------------------------
    @DebugOnly
    override fun isDebugLogEnabled(): Boolean =
        sharedPreferences.getBoolean(SP_KEY_DEBUG_LOG_ENABLED, BuildConfig.IS_STAGING)
            .also { config.setCollectDebugLogs(it) }

    @DebugOnly
    override fun enableDebugLog(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(SP_KEY_DEBUG_LOG_ENABLED, isEnabled)
            .also { config.setCollectDebugLogs(isEnabled) }
            .apply()
    }

    @DebugOnly
    override fun switchDebugLogEnabled() {
        val currentFlag = isDebugLogEnabled()
        enableDebugLog(isEnabled = !currentFlag)
    }

    @DebugOnly
    override fun testBackup() {
        Timber.d("Test Backup: accessToken=${accessToken()}, userId=${currentUserId()}, %s backup[%s]",
            "lastActionTime=${getLastActionTime()}, themeId=${getThemeResId()}, debugLog=${isDebugLogEnabled()}",
                "privateKey=${getPrivateKey()}, referralId=${getReferralCode()}")
    }

    override fun isDeveloperModeEnabled(): Boolean =
        sharedPreferences.getBoolean(SP_KEY_DEVELOPER_MODE, BuildConfig.IS_STAGING)
            .also { config.setDeveloperMode(it) }

    override fun enableDeveloperMode() {
        sharedPreferences.edit().putBoolean(SP_KEY_DEVELOPER_MODE, true)
            .also { config.setDeveloperMode(true) }
            .apply()
    }

    override fun switchDeveloperMode() {
        val currentFlag = sharedPreferences.getBoolean(SP_KEY_DEVELOPER_MODE, BuildConfig.IS_STAGING)
        sharedPreferences.edit().putBoolean(SP_KEY_DEVELOPER_MODE, !currentFlag)
            .also { config.setDeveloperMode(!currentFlag) }
            .apply()
    }

    /* Auth */
    // --------------------------------------------------------------------------------------------
    override fun accessToken(): AccessToken? =
        sharedPreferences
            .takeIf { it.contains(SP_KEY_AUTH_ACCESS_TOKEN) }
            ?.let { sp ->
                currentUserId()?.let { userId ->
                    sp.getString(SP_KEY_AUTH_ACCESS_TOKEN, null)
                        ?.let { AccessToken(userId = userId, accessToken = it) }
                }
            }

    override fun currentUserId(): String? =
        sharedPreferences
            .takeIf { it.contains(SP_KEY_AUTH_USER_ID) }
            ?.let { it.getString(SP_KEY_AUTH_USER_ID, null) }

    override fun currentUserCreateTs(): Long =
        sharedPreferences.getLong(SP_KEY_AUTH_USER_CREATE_TS, 0L)

    override fun currentUserGender(): Gender =
        Gender.from(sharedPreferences.getString(SP_KEY_AUTH_USER_GENDER, "") ?: "")

    override fun currentUserYearOfBirth(): Int =
        sharedPreferences.getInt(SP_KEY_AUTH_USER_YEAR_OF_BIRTH, DomainUtil.UNKNOWN_VALUE)

    override fun hasUserCreateTs(): Boolean = currentUserCreateTs() != 0L

    override fun hasUserGender(): Boolean = currentUserGender() != Gender.UNKNOWN

    override fun hasUserYearOfBirth(): Boolean = currentUserYearOfBirth() > DomainUtil.UNKNOWN_VALUE

    override fun saveUserProfile(userId: String, userGender: Gender, userYearOfBirth: Int, accessToken: String) {
        sharedPreferences.edit()
            .putString(SP_KEY_AUTH_USER_ID, userId)
            .putLong(SP_KEY_AUTH_USER_CREATE_TS, System.currentTimeMillis())
            .putString(SP_KEY_AUTH_USER_GENDER, userGender.string)
            .putInt(SP_KEY_AUTH_USER_YEAR_OF_BIRTH, userYearOfBirth)
            .putString(SP_KEY_AUTH_ACCESS_TOKEN, accessToken)
            .apply()
    }

    override fun deleteUserProfile(userId: String) {
        sharedPreferences.edit()
            .remove(SP_KEY_AUTH_USER_ID)
            .remove(SP_KEY_AUTH_USER_CREATE_TS)
            .remove(SP_KEY_AUTH_USER_GENDER)
            .remove(SP_KEY_AUTH_USER_YEAR_OF_BIRTH)
            .remove(SP_KEY_AUTH_ACCESS_TOKEN)
            .apply()
    }

    // ------------------------------------------
    override fun onLogout() {
        deleteLocation()
        dropBigEditText()  // forget saved input text for dialog
        dropFilters()  // forget saved filters on logout
        dropUserProfileProperties()  // forget profile properties for previous user
        dropUserProfileCustomPropertiesUnsavedInput()  // forget unsaved input profile properties
        sharedPreferences.edit().putBoolean(SP_KEY_FLAG_NEED_SHOW_FILTERS, true).apply()
    }

    /* Filters */
    // --------------------------------------------------------------------------------------------
    private fun checkAndFixFilters() {
        val filters = getFilters()
        filters
            .takeIf { it != NoFilters }
            ?.let { Filters.createWithAgeRange(it) }
            ?.let { fixupFilters ->
                // if filters out of bounds - use unconstrained filters
                if (fixupFilters == EmptyFilters) {
                    setFilters(NoFilters)
                    return@let
                }
                // filters has been fixed up to respect boundaries
                if (fixupFilters != filters) {
                    setFilters(fixupFilters)
                }
            }
    }

    override fun hasFiltersApplied(): Boolean = getFilters() != NoFilters

    override fun getFilters(): Filters =
        sharedPreferences.getString(SP_KEY_FILTERS, null)
            ?.let { Gson().fromJson(it, Filters::class.java) }
            ?: NoFilters

    override fun setFilters(filters: Filters) {
        sharedPreferences.edit().putString(SP_KEY_FILTERS, filters.toJson()).apply()
    }

    override fun dropFilters() {
        sharedPreferences.edit().remove(SP_KEY_FILTERS).apply()
    }

    /* Location */
    // --------------------------------------------------------------------------------------------
    override fun getLocation(): GpsLocation? {
        val latitudeStr = sharedPreferences.getString(SP_KEY_LOCATION_LATITUDE, null)
        val longitudeStr = sharedPreferences.getString(SP_KEY_LOCATION_LONGITUDE, null)
        if (latitudeStr.isNullOrBlank() || longitudeStr.isNullOrBlank()) {
            return null
        }

        val latitude = latitudeStr.toDouble()
        val longitude = longitudeStr.toDouble()
        return if (abs(latitude) <= LOCATION_EPS && abs(longitude) <= LOCATION_EPS) null
               else GpsLocation(latitude, longitude)
    }

    override fun saveLocation(location: GpsLocation) {
        if (abs(location.latitude) <= LOCATION_EPS && abs(location.longitude) <= LOCATION_EPS) {
            deleteLocation()  // location is near (0, 0) point, so delete it
        } else {
            sharedPreferences.edit()
                .putString(SP_KEY_LOCATION_LATITUDE, "${location.latitude}")
                .putString(SP_KEY_LOCATION_LONGITUDE, "${location.longitude}")
                .apply()
        }
    }

    override fun deleteLocation() {
        sharedPreferences.edit()
            .remove(SP_KEY_LOCATION_LATITUDE)
            .remove(SP_KEY_LOCATION_LONGITUDE)
            .apply()
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------
    internal fun getLastActionTime(): Long = sharedPreferences.getLong(SP_KEY_LAST_ACTION_TIME, 0L)

    internal fun saveLastActionTime(lastActionTime: Long) {
        sharedPreferences.edit()
            .putLong(SP_KEY_LAST_ACTION_TIME, lastActionTime)
            .apply()
    }

    internal fun deleteLastActionTime() {
        sharedPreferences.edit().remove(SP_KEY_LAST_ACTION_TIME).apply()
    }

    /* Referral program */
    // --------------------------------------------------------------------------------------------
    override fun createPrivateKeyIfNotExists(): String =
        getPrivateKey() ?: randomString().also { setPrivateKey(it) }

    override fun getPrivateKey(): String? = backupSharedPreferences.getString(SP_KEY_PRIVATE_KEY, null)

    override fun hasPrivateKey(): Boolean = !backupSharedPreferences.getString(SP_KEY_PRIVATE_KEY, null).isNullOrBlank()

    override fun setPrivateKey(privateKey: String?) {
        backupSharedPreferences.edit().putString(SP_KEY_PRIVATE_KEY, privateKey).apply()
    }

    override fun getReferralCode(): String? = backupSharedPreferences.getString(SP_KEY_REFERRAL_CODE, null)

    override fun hasReferralCode(): Boolean = !backupSharedPreferences.getString(SP_KEY_REFERRAL_CODE, null).isNullOrBlank()

    override fun setReferralCode(code: String?, dontOverride: Boolean) {
        if (backupSharedPreferences.getBoolean(SP_KEY_REFERRAL_CODE_DONT_OVERRIDE, false)) {
            backupSharedPreferences.edit().remove(SP_KEY_REFERRAL_CODE_DONT_OVERRIDE).apply()
            return  // don't override, if flag is set
        }

        backupSharedPreferences.edit()
            .putString(SP_KEY_REFERRAL_CODE, code)
            .putBoolean(SP_KEY_REFERRAL_CODE_DONT_OVERRIDE, dontOverride)
            .apply()
    }

    /* User Settings */
    // --------------------------------------------------------------------------------------------
    override fun getUserSettingDailyPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_DAILY_PUSH_ENABLED, true)
    override fun getUserSettingLikesPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_LIKES_PUSH_ENABLED, true)
    override fun getUserSettingMatchesPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_MATCHES_PUSH_ENABLED, true)
    override fun getUserSettingMessagesPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_MESSAGES_PUSH_ENABLED, true)
    override fun getUserSettingVibrationPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_VIBRATION_PUSH_ENABLED, true)

    override fun setUserSettingDailyPushEnabled(pushEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(SP_KEY_USER_SETTINGS_DAILY_PUSH_ENABLED, pushEnabled).apply()
    }

    override fun setUserSettingLikesPushEnabled(pushEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(SP_KEY_USER_SETTINGS_LIKES_PUSH_ENABLED, pushEnabled).apply()
    }

    override fun setUserSettingMatchesPushEnabled(pushEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(SP_KEY_USER_SETTINGS_MATCHES_PUSH_ENABLED, pushEnabled).apply()
    }

    override fun setUserSettingMessagesPushEnabled(pushEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(SP_KEY_USER_SETTINGS_MESSAGES_PUSH_ENABLED, pushEnabled).apply()
    }

    override fun setUserSettingVibrationPushEnabled(pushEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(SP_KEY_USER_SETTINGS_VIBRATION_PUSH_ENABLED, pushEnabled).apply()
    }

    // ------------------------------------------
    override fun getUserProfileProperties(): UserProfilePropertiesRaw =
        with (sharedPreferences) {
            UserProfilePropertiesRaw(
                children = getInt(SP_KEY_USER_PROFILE_PROPERTY_CHILDREN, DomainUtil.UNKNOWN_VALUE),
                education = getInt(SP_KEY_USER_PROFILE_PROPERTY_EDUCATION, DomainUtil.UNKNOWN_VALUE),
                hairColor = getInt(SP_KEY_USER_PROFILE_PROPERTY_HAIR_COLOR, DomainUtil.UNKNOWN_VALUE),
                height = getInt(SP_KEY_USER_PROFILE_PROPERTY_HEIGHT, DomainUtil.UNKNOWN_VALUE),
                income = getInt(SP_KEY_USER_PROFILE_PROPERTY_INCOME, DomainUtil.UNKNOWN_VALUE),
                property = getInt(SP_KEY_USER_PROFILE_PROPERTY_PROPERTY, DomainUtil.UNKNOWN_VALUE),
                transport = getInt(SP_KEY_USER_PROFILE_PROPERTY_TRANSPORT, DomainUtil.UNKNOWN_VALUE),
                about = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_ABOUT, "") ?: "",
                company = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_COMPANY, "") ?: "",
                jobTitle = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_JOB_TITLE, "") ?: "",
                name = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_NAME, "") ?: "",
                statusText = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_STATUS_TEXT, "") ?: "",
                socialInstagram = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_INSTAGRAM, "") ?: "",
                socialTikTok = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_TIKTOK, "") ?: "",
                university = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_UNIVERSITY, "") ?: "",
                whereFrom = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_FROM, "") ?: "",
                whereLive = getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_LIVE, "") ?: "")
        }

    override fun setUserProfileProperties(propertiesRaw: UserProfilePropertiesRaw) {
        sharedPreferences.edit()
            .putInt(SP_KEY_USER_PROFILE_PROPERTY_CHILDREN, propertiesRaw.children)
            .putInt(SP_KEY_USER_PROFILE_PROPERTY_EDUCATION, propertiesRaw.education)
            .putInt(SP_KEY_USER_PROFILE_PROPERTY_HAIR_COLOR, propertiesRaw.hairColor)
            .putInt(SP_KEY_USER_PROFILE_PROPERTY_HEIGHT, propertiesRaw.height)
            .putInt(SP_KEY_USER_PROFILE_PROPERTY_INCOME, propertiesRaw.income)
            .putInt(SP_KEY_USER_PROFILE_PROPERTY_PROPERTY, propertiesRaw.property)
            .putInt(SP_KEY_USER_PROFILE_PROPERTY_TRANSPORT, propertiesRaw.transport)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_ABOUT, propertiesRaw.about)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_COMPANY, propertiesRaw.company)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_JOB_TITLE, propertiesRaw.jobTitle)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_NAME, propertiesRaw.name)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_STATUS_TEXT, propertiesRaw.statusText)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_INSTAGRAM, propertiesRaw.socialInstagram)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_TIKTOK, propertiesRaw.socialTikTok)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_UNIVERSITY, propertiesRaw.university)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_FROM, propertiesRaw.whereFrom)
            .putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_LIVE, propertiesRaw.whereLive)
            .apply()
    }

    override fun dropUserProfileProperties() {
        sharedPreferences.edit()
            .remove(SP_KEY_USER_PROFILE_PROPERTY_CHILDREN)
            .remove(SP_KEY_USER_PROFILE_PROPERTY_EDUCATION)
            .remove(SP_KEY_USER_PROFILE_PROPERTY_HAIR_COLOR)
            .remove(SP_KEY_USER_PROFILE_PROPERTY_HEIGHT)
            .remove(SP_KEY_USER_PROFILE_PROPERTY_INCOME)
            .remove(SP_KEY_USER_PROFILE_PROPERTY_PROPERTY)
            .remove(SP_KEY_USER_PROFILE_PROPERTY_TRANSPORT)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_ABOUT)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_COMPANY)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_JOB_TITLE)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_NAME)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_STATUS_TEXT)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_INSTAGRAM)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_SOCIAL_TIKTOK)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_UNIVERSITY)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_FROM)
            .remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTY_WHERE_LIVE)
            .apply()
    }

    override fun getUserProfileCustomPropertiesUnsavedInput(): UserProfileCustomPropertiesUnsavedInput =
        sharedPreferences.getString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTIES_UNSAVED_INPUT, null)
            ?.let { Gson().fromJson(it, UserProfileCustomPropertiesUnsavedInput::class.java) }
            ?: UserProfileCustomPropertiesUnsavedInput()

    override fun setUserProfileCustomPropertiesUnsavedInput(unsavedInput: UserProfileCustomPropertiesUnsavedInput) {
        sharedPreferences.edit().putString(SP_KEY_USER_PROFILE_CUSTOM_PROPERTIES_UNSAVED_INPUT, unsavedInput.toJson()).apply()
    }

    override fun dropUserProfileCustomPropertiesUnsavedInput() {
        sharedPreferences.edit().remove(SP_KEY_USER_PROFILE_CUSTOM_PROPERTIES_UNSAVED_INPUT).apply()
    }

    /* Misc */
    // --------------------------------------------------------------------------------------------
    override fun getBigEditText(): String = sharedPreferences.getString(SP_KEY_BIG_EDIT_TEXT, "") ?: ""

    override fun setBigEditText(text: String) {
        sharedPreferences.edit().putString(SP_KEY_BIG_EDIT_TEXT, text).apply()
    }

    override fun dropBigEditText() {
        sharedPreferences.edit().remove(SP_KEY_BIG_EDIT_TEXT).apply()
    }

    // ------------------------------------------
    override fun needShowFilters(): Boolean =
        sharedPreferences.let {
            val flag = it.getBoolean(SP_KEY_FLAG_NEED_SHOW_FILTERS, true)
            it.edit().putBoolean(SP_KEY_FLAG_NEED_SHOW_FILTERS, false).apply()
            flag
        }
}

// --------------------------------------------------------------------------------------------
inline fun ISharedPrefsManager.accessCompletable(dontWarn: Boolean = false, body: (it: AccessToken) -> Completable): Completable =
    accessToken()
        ?.let { body(it) }
        ?: run {
            val rootCause = InvalidAccessTokenException()
            val e = if (dontWarn) SilentFatalException(rootCause) else rootCause
            Completable.error(e)
        }

inline fun <reified T> ISharedPrefsManager.accessMaybe(dontWarn: Boolean = false, body: (it: AccessToken) -> Maybe<T>): Maybe<T> =
    accessToken()
        ?.let { body(it) }
        ?: run {
            val rootCause = InvalidAccessTokenException()
            val e = if (dontWarn) SilentFatalException(rootCause) else rootCause
            Maybe.error<T>(e)
        }

inline fun <reified T> ISharedPrefsManager.accessSingle(dontWarn: Boolean = false, body: (it: AccessToken) -> Single<T>): Single<T> =
    accessToken()
        ?.let { body(it) }
        ?: run {
            val rootCause = InvalidAccessTokenException()
            val e = if (dontWarn) SilentFatalException(rootCause) else rootCause
            Single.error<T>(e)
        }

inline fun <reified T> ISharedPrefsManager.accessFlowable(dontWarn: Boolean = false, body: (it: AccessToken) -> Flowable<T>): Flowable<T> =
    accessToken()
        ?.let { body(it) }
        ?: run {
            val rootCause = InvalidAccessTokenException()
            val e = if (dontWarn) SilentFatalException(rootCause) else rootCause
            Flowable.error<T>(e)
        }

inline fun <reified T> ISharedPrefsManager.accessObservable(dontWarn: Boolean = false, body: (it: AccessToken) -> Observable<T>): Observable<T> =
    accessToken()
        ?.let { body(it) }
        ?: run {
            val rootCause = InvalidAccessTokenException()
            val e = if (dontWarn) SilentFatalException(rootCause) else rootCause
            Observable.error<T>(e)
        }
