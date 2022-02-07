package com.example.camera.presentation.detection.info

import com.example.camera.processing.Settings

class SettingsInfo(
    val resolution: String,
    val maxDetections: String,
    val confidenceThreshold: String,
    val imageQuality: String,
    val inferenceType: String,
    val serverAddress: String
) {
    companion object {
        const val INFERENCE_LOCAL = "Local"
        const val INFERENCE_REMOTE = "Remote"
    }

    val showAddress: Boolean get() = inferenceType == INFERENCE_REMOTE
}

fun Settings.toSettingsInfo() = SettingsInfo(
    resolution = imageSize.toString(),
    maxDetections = maxDetections.toString(),
    confidenceThreshold = detectionThreshold.toString(),
    imageQuality = imageQuality.toString(),
    inferenceType = if (localInference) SettingsInfo.INFERENCE_LOCAL else SettingsInfo.INFERENCE_REMOTE,
    serverAddress = serverAddressFull
)
