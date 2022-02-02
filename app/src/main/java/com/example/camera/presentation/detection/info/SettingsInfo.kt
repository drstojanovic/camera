package com.example.camera.presentation.detection.info

import com.example.camera.processing.Settings

class SettingsInfo(
    val resolution: String,
    val maxDetections: String,
    val confidenceThreshold: String,
    val imageQuality: String,
    val inferenceType: String,
    val serverAddress: String
)

fun Settings.toSettingsInfo() = SettingsInfo(
    resolution = imageSize.toString(),
    maxDetections = maxDetections.toString(),
    confidenceThreshold = confidenceThreshold.toString(),
    imageQuality = imageQuality.toString(),
    inferenceType = if (localInference) "Local" else "Remote",
    serverAddress = serverAddressFull
)
