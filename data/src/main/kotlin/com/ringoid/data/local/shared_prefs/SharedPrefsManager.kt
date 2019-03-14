package com.ringoid.data.local.shared_prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StyleRes
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.exception.InvalidAccessTokenException
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.domain.repository.ISharedPrefsManager
import io.reactivex.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsManager @Inject constructor(context: Context) : ISharedPrefsManager {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        const val SHARED_PREFS_FILE_NAME = "Ringoid.prefs"

        const val SP_KEY_THEME = "sp_key_theme"
        @DebugOnly const val SP_KEY_DEBUG_LOG_ENABLED = "sp_key_debug_log_enabled"

        /* Auth */
        // --------------------------------------
        const val SP_KEY_AUTH_USER_ID = "sp_key_auth_user_id"
        const val SP_KEY_AUTH_ACCESS_TOKEN = "sp_key_auth_access_token"

        /* Actions */
        // --------------------------------------
        const val SP_KEY_LAST_ACTION_TIME = "sp_key_last_action_time"
    }

    // --------------------------------------------------------------------------------------------
    @StyleRes
    override fun getThemeResId(@StyleRes defaultThemeResId: Int): Int = sharedPreferences.getInt(SP_KEY_THEME, defaultThemeResId)

    override fun saveThemeResId(@StyleRes themeResId: Int) {
        sharedPreferences.edit().putInt(SP_KEY_THEME, themeResId).apply()
    }

    // ------------------------------------------
    @DebugOnly
    override fun isDebugLogEnabled(): Boolean =
        BuildConfig.IS_STAGING && sharedPreferences.getBoolean(SP_KEY_DEBUG_LOG_ENABLED, false)

    @DebugOnly
    override fun switchDebugLogEnabled() {
        val currentFlag = isDebugLogEnabled()
        sharedPreferences.edit().putBoolean(SP_KEY_DEBUG_LOG_ENABLED, !currentFlag).apply()
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

    override fun saveUserProfile(userId: String, accessToken: String) {
        sharedPreferences.edit()
            .putString(SP_KEY_AUTH_USER_ID, userId)
            .putString(SP_KEY_AUTH_ACCESS_TOKEN, accessToken)
            .apply()
    }

    override fun deleteUserProfile(userId: String) {
        sharedPreferences.edit()
            .remove(SP_KEY_AUTH_USER_ID)
            .remove(SP_KEY_AUTH_ACCESS_TOKEN)
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
