package com.example.camera.repository

import com.example.camera.processing.Settings
import com.example.camera.repository.model.SettingsRaw
import com.example.camera.repository.model.toSettings
import com.example.camera.repository.model.toSettingsRaw
import com.example.camera.utils.SHARED_PREFS_SETTINGS_CLASSIFICATION
import com.example.camera.utils.SHARED_PREFS_SETTINGS_DETECTION
import com.example.camera.utils.SharedPrefsUtils
import com.example.camera.utils.tryToExecute

object SettingsRepository {

    suspend fun getClassificationSettings(): Result<Settings> =
        tryToExecute {
            (SharedPrefsUtils.getObject<SettingsRaw>(SHARED_PREFS_SETTINGS_CLASSIFICATION) ?: SettingsRaw())
                .toSettings()
        }

    suspend fun getDetectionSettings(): Result<Settings> =
        tryToExecute {
            (SharedPrefsUtils.getObject<SettingsRaw>(SHARED_PREFS_SETTINGS_DETECTION) ?: SettingsRaw())
                .toSettings()
        }

    suspend fun storeClassificationSettings(settings: Settings): Result<Unit> =
        tryToExecute {
            SharedPrefsUtils.putObject(SHARED_PREFS_SETTINGS_CLASSIFICATION, settings.toSettingsRaw())
        }

    suspend fun storeDetectionSettings(settings: Settings): Result<Unit> =
        tryToExecute {
            SharedPrefsUtils.putObject(SHARED_PREFS_SETTINGS_DETECTION, settings.toSettingsRaw())
        }
}
