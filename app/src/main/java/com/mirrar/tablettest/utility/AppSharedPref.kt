package com.mirrar.tablettest.utility

import android.app.Activity
import android.content.Context

class AppSharedPref(activity: Activity, key: String = "user_data") {

    private val sharedPreferences = activity.getSharedPreferences(key, Context.MODE_PRIVATE)

    fun putString(tag: String, value: String?) {
        sharedPreferences.edit().putString(tag, value).apply()
    }

    fun getString(tag: String): String? {
        return sharedPreferences.getString(tag, null)
    }

    fun putBoolean(tag: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(tag, value).apply()
    }

    fun getBoolean(tag: String): Boolean {
        return sharedPreferences.getBoolean(tag, false)
    }

    fun putInt(tag: String, value: Int) {
        sharedPreferences.edit().putInt(tag, value).apply()
    }

    fun getInt(tag: String): Int {
        return sharedPreferences.getInt(tag, -1)
    }

    fun removeKey(tag: String) {
        sharedPreferences.edit().remove(tag).apply()
    }
}