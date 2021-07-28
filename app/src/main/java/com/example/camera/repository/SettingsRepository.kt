package com.example.camera.repository

import com.example.camera.processing.Settings
import com.example.camera.repository.model.SettingsRaw
import com.example.camera.repository.model.toSettings
import com.example.camera.repository.model.toSettingsRaw
import com.example.camera.utils.SHARED_PREFS_SETTINGS
import com.example.camera.utils.SharedPrefsUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class SettingsRepository {

    fun getSettings(): Single<Settings> =
        Single.fromCallable { SharedPrefsUtils.getObject<SettingsRaw>(SHARED_PREFS_SETTINGS) ?: SettingsRaw() }
            .map { it.toSettings() }
            .subscribeOn(Schedulers.io())

    fun storeSettings(settings: Settings): Completable =
        Completable.fromCallable { SharedPrefsUtils.putObject(SHARED_PREFS_SETTINGS, settings.toSettingsRaw()) }
            .subscribeOn(Schedulers.io())
}
