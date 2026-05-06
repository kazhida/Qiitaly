package com.abplus.qiitaly

import platform.Foundation.NSUserDefaults

class QiitaTokenPreferences {
    private val preferences = NSUserDefaults.standardUserDefaults

    fun saveAccessToken(accessToken: String) {
        preferences.setObject(accessToken, KEY_ACCESS_TOKEN)
    }

    fun getAccessToken(): String? {
        return preferences.stringForKey(KEY_ACCESS_TOKEN)
    }

    fun clearAccessToken() {
        preferences.removeObjectForKey(KEY_ACCESS_TOKEN)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}
