package com.abplus.qiitaly

import android.content.Context
import androidx.core.content.edit

class QiitaTokenPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAccessToken(accessToken: String) {
        preferences.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
        }
    }

    fun getAccessToken(): String? {
        return preferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearAccessToken() {
        preferences.edit {
            remove(KEY_ACCESS_TOKEN)
        }
    }

    companion object {
        private const val PREFS_NAME = "qiita_auth"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}
