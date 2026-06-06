package com.homefood.delivery.data.session

import android.content.Context

/**
 * Tiny persistence for "who is logged in".
 *
 * NOTE: the current backend has no token-based auth — login just returns the
 * user record — so for this MVP we store the userId locally. When the backend
 * adds JWT, store the token here instead and attach it via an OkHttp interceptor.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("homefood_session", Context.MODE_PRIVATE)

    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, 0)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()

    var fullName: String?
        get() = prefs.getString(KEY_NAME, null)
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var zoneId: Int
        get() = prefs.getInt(KEY_ZONE_ID, 0)
        set(value) = prefs.edit().putInt(KEY_ZONE_ID, value).apply()

    val isLoggedIn: Boolean get() = userId != 0

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "full_name"
        const val KEY_ZONE_ID = "zone_id"
    }
}
