package com.example.camera.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.camera.CameraApp
import com.squareup.moshi.Moshi

object SharedPrefsUtils {

    private const val PREFS_NAME = "shared_prefs"
    private var sharedPreferences: SharedPreferences? = null
    val moshi: Moshi by lazy { Moshi.Builder().build() }

    private val instance: SharedPreferences?
        get() {
            if (sharedPreferences == null) {
                sharedPreferences = CameraApp.appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
            return sharedPreferences
        }

    fun putString(key: String?, value: String?) =
        instance?.edit()?.putString(key, value)?.apply()

    fun getString(key: String?): String? =
        instance?.getString(key, null)

    fun putBoolean(key: String?, value: Boolean) =
        instance?.edit()?.putBoolean(key, value)?.apply()

    fun getBoolean(key: String?): Boolean =
        instance?.getBoolean(key, false) ?: false

    fun putInteger(key: String?, value: Int) =
        instance?.edit()?.putInt(key, value)?.apply()

    fun getInteger(key: String?): Int =
        instance?.getInt(key, -1) ?: -1

    inline fun <reified T> putObject(key: String?, obj: T) =
        putString(key, moshi.adapter(T::class.java).toJson(obj))

    inline fun <reified T> getObject(key: String?): T? =
        getString(key)?.let { moshi.adapter(T::class.java).fromJson(it) }
}