package com.example.camera.domain

import com.example.camera.processing.Settings
import com.example.camera.repository.SettingsRepository

class GetSettings {

    suspend fun execute(isClassificationMode: Boolean): Result<Settings> = if (isClassificationMode) {
        SettingsRepository.getClassificationSettings()
            .map { it.localInference = false; it }
    } else {
        SettingsRepository.getDetectionSettings()
    }
}
