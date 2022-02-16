package com.example.camera.domain

import com.example.camera.processing.Settings
import com.example.camera.repository.SettingsRepository
import io.reactivex.Single

class GetSettings {

    fun execute(isClassificationMode: Boolean): Single<Settings> = if (isClassificationMode) {
        SettingsRepository.getClassificationSettings()
            .map { it.localInference = false; it }
    } else {
        SettingsRepository.getDetectionSettings()
    }
}
