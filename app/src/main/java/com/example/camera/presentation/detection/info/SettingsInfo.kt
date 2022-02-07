package com.example.camera.presentation.detection.info

import com.example.camera.processing.Settings
import com.example.camera.utils.INFERENCE_TYPE_LOCAL
import com.example.camera.utils.INFERENCE_TYPE_REMOTE

class SettingsInfo(
    val resolution: String,
    val maxDetections: String,
    val confidenceThreshold: String,
    val imageQuality: String,
    val inferenceType: String,
    val serverAddress: String
) {
    val showAddress: Boolean get() = inferenceType == INFERENCE_TYPE_REMOTE
}

fun Settings.toSettingsInfo() = SettingsInfo(
    resolution = imageSize.toString(),
    maxDetections = maxDetections.toString(),
    confidenceThreshold = detectionThreshold.toString(),
    imageQuality = imageQuality.toString(),
    inferenceType = if (localInference) INFERENCE_TYPE_LOCAL else INFERENCE_TYPE_REMOTE,
    serverAddress = serverAddressFull
)
