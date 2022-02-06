package com.example.camera.domain

import com.example.camera.processing.Settings
import com.example.camera.repository.SettingsRepository

class StoreSettings {

    fun execute(settings: Settings, isClassification: Boolean) = if (isClassification) {
        SettingsRepository.storeClassificationSettings(settings)
    } else {
        SettingsRepository.storeDetectionSettings(settings)
    }
}
