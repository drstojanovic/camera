package com.example.camera.repository

import com.example.camera.processing.Settings
import com.example.camera.repository.model.SettingsRaw
import com.example.camera.repository.model.toSettings
import com.example.camera.repository.model.toSettingsRaw
import com.example.camera.utils.SHARED_PREFS_SETTINGS_CLASSIFICATION
import com.example.camera.utils.SHARED_PREFS_SETTINGS_DETECTION
import com.example.camera.utils.SharedPrefsUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

object SettingsRepository {

    fun getClassificationSettings(): Single<Settings> =
        Single.fromCallable {
            SharedPrefsUtils.getObject<SettingsRaw>(SHARED_PREFS_SETTINGS_CLASSIFICATION) ?: SettingsRaw()
        }
            .map { it.toSettings() }
            .subscribeOn(Schedulers.io())

    fun getDetectionSettings(): Single<Settings> =
        Single.fromCallable {
            SharedPrefsUtils.getObject<SettingsRaw>(SHARED_PREFS_SETTINGS_DETECTION) ?: SettingsRaw()
        }
            .map { it.toSettings() }
            .subscribeOn(Schedulers.io())

    fun storeClassificationSettings(settings: Settings): Completable =
        Completable.fromCallable {
            SharedPrefsUtils.putObject(SHARED_PREFS_SETTINGS_CLASSIFICATION, settings.toSettingsRaw())
        }
            .subscribeOn(Schedulers.io())

    fun storeDetectionSettings(settings: Settings): Completable =
        Completable.fromCallable {
            SharedPrefsUtils.putObject(SHARED_PREFS_SETTINGS_DETECTION, settings.toSettingsRaw())
        }
            .subscribeOn(Schedulers.io())
}
