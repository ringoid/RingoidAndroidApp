package com.ringoid.data.local.shared_prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StyleRes
import com.ringoid.data.manager.RuntimeConfig
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.exception.InvalidAccessTokenException
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.utility.LOCATION_EPS
import com.ringoid.utility.randomString
import io.reactivex.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

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
    }

    companion object {
        private const val SHARED_PREFS_FILE_NAME = "Ringoid.prefs"
        private const val BACKUP_SHARED_PREFS_FILE_NAME = "RingoidBackup.prefs"

        private const val SP_KEY_BUILD_CODE = "sp_key_build_code"
        private const val SP_KEY_APP_UID = "sp_key_app_uid"
        private const val SP_KEY_THEME = "sp_key_theme"
        @DebugOnly private const val SP_KEY_DEBUG_LOG_ENABLED = "sp_key_debug_log_enabled"
        @DebugOnly private const val SP_KEY_DEVELOPER_MODE = "sp_key_developer_mode"

        /* Auth */
        // --------------------------------------
        const val SP_KEY_AUTH_USER_ID = "sp_key_auth_user_id"
        const val SP_KEY_AUTH_USER_GENDER = "sp_key_auth_user_gender"
        const val SP_KEY_AUTH_USER_YEAR_OF_BIRTH = "sp_key_auth_user_year_of_birth"
        const val SP_KEY_AUTH_ACCESS_TOKEN = "sp_key_auth_access_token"

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

        /* User Settings */
        // --------------------------------------
        const val SP_KEY_USER_SETTINGS_DAILY_PUSH_ENABLED = "sp_key_user_settings_daily_push_enabled"
        const val SP_KEY_USER_SETTINGS_LIKES_PUSH_ENABLED = "sp_key_user_settings_likes_push_enabled"
        const val SP_KEY_USER_SETTINGS_MATCHES_PUSH_ENABLED = "sp_key_user_settings_matches_push_enabled"
        const val SP_KEY_USER_SETTINGS_MESSAGES_PUSH_ENABLED = "sp_key_user_settings_messages_push_enabled"
    }

    // --------------------------------------------------------------------------------------------
    override fun getAppUid(): String =
        sharedPreferences.getString(SP_KEY_APP_UID, null)
            ?: run { randomString().also { sharedPreferences.edit().putString(SP_KEY_APP_UID, it).apply() } }

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
    override fun switchDebugLogEnabled() {
        val currentFlag = isDebugLogEnabled()
        sharedPreferences.edit().putBoolean(SP_KEY_DEBUG_LOG_ENABLED, !currentFlag)
            .also { config.setCollectDebugLogs(!currentFlag) }
            .apply()
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

    override fun currentUserGender(): Gender =
        Gender.from(sharedPreferences.getString(SP_KEY_AUTH_USER_GENDER, "") ?: "")

    override fun currentUserYearOfBirth(): Int =
        sharedPreferences.getInt(SP_KEY_AUTH_USER_YEAR_OF_BIRTH, DomainUtil.BAD_VALUE)

    override fun saveUserProfile(userId: String, userGender: Gender, userYearOfBirth: Int, accessToken: String) {
        sharedPreferences.edit()
            .putString(SP_KEY_AUTH_USER_ID, userId)
            .putString(SP_KEY_AUTH_USER_GENDER, userGender.string)
            .putInt(SP_KEY_AUTH_USER_YEAR_OF_BIRTH, userYearOfBirth)
            .putString(SP_KEY_AUTH_ACCESS_TOKEN, accessToken)
            .apply()
    }

    override fun deleteUserProfile(userId: String) {
        sharedPreferences.edit()
            .remove(SP_KEY_AUTH_USER_ID)
            .remove(SP_KEY_AUTH_USER_GENDER)
            .remove(SP_KEY_AUTH_USER_YEAR_OF_BIRTH)
            .remove(SP_KEY_AUTH_ACCESS_TOKEN)
            .apply()
    }

    /* Location */
    // --------------------------------------------------------------------------------------------
    override fun getLocation(): GpsLocation? {
        val latitudeStr = sharedPreferences.getString(SP_KEY_LOCATION_LATITUDE, null)
        val longitudeStr = sharedPreferences.getString(SP_KEY_LOCATION_LONGITUDE, null)
        if (latitudeStr.isNullOrBlank() || longitudeStr.isNullOrBlank()) {
            return null
        }

        val latitude = latitudeStr!!.toDouble()
        val longitude = longitudeStr!!.toDouble()
        return if (Math.abs(latitude) <= LOCATION_EPS && Math.abs(longitude) <= LOCATION_EPS) null
               else GpsLocation(latitude, longitude)
    }

    override fun saveLocation(location: GpsLocation) {
        if (Math.abs(location.latitude) <= LOCATION_EPS && Math.abs(location.longitude) <= LOCATION_EPS) {
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

    override fun setReferralCode(code: String?) {
        backupSharedPreferences.edit().putString(SP_KEY_REFERRAL_CODE, code).apply()
    }

    /* User Settings */
    // --------------------------------------------------------------------------------------------
    override fun getUserSettingDailyPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_DAILY_PUSH_ENABLED, true)
    override fun getUserSettingLikesPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_LIKES_PUSH_ENABLED, true)
    override fun getUserSettingMatchesPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_MATCHES_PUSH_ENABLED, true)
    override fun getUserSettingMessagesPushEnabled(): Boolean = sharedPreferences.getBoolean(SP_KEY_USER_SETTINGS_MESSAGES_PUSH_ENABLED, true)

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
}

// --------------------------------------------------------------------------------------------
inline fun ISharedPrefsManager.accessCompletable(body: (it: AccessToken) -> Completable): Completable =
    accessToken()?.let { body(it) } ?: Completable.error { InvalidAccessTokenException() }

inline fun <reified T> ISharedPrefsManager.accessMaybe(body: (it: AccessToken) -> Maybe<T>): Maybe<T> =
    accessToken()?.let { body(it) } ?: Maybe.error<T> { InvalidAccessTokenException() }

inline fun <reified T> ISharedPrefsManager.accessSingle(body: (it: AccessToken) -> Single<T>): Single<T> =
    accessToken()?.let { body(it) } ?: Single.error<T> { InvalidAccessTokenException() }

inline fun <reified T> ISharedPrefsManager.accessFlowable(body: (it: AccessToken) -> Flowable<T>): Flowable<T> =
    accessToken()?.let { body(it) } ?: Flowable.error<T> { InvalidAccessTokenException() }

inline fun <reified T> ISharedPrefsManager.accessObservable(body: (it: AccessToken) -> Observable<T>): Observable<T> =
    accessToken()?.let { body(it) } ?: Observable.error<T> { InvalidAccessTokenException() }
