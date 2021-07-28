package com.example.camera.repository.model

import com.example.camera.processing.Settings
import com.example.camera.utils.DEFAULTS_CONFIDENCE_THRESHOLD
import com.example.camera.utils.DEFAULTS_IMAGE_HEIGHT
import com.example.camera.utils.DEFAULTS_IMAGE_WIDTH
import com.example.camera.utils.DEFAULTS_MAX_DETECTIONS
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SettingsRaw(
    val serverIpAddress: String? = null,
    val serverPort: String? = null,
    val maxDetections: Int = DEFAULTS_MAX_DETECTIONS,
    val confidenceThreshold: Int = DEFAULTS_CONFIDENCE_THRESHOLD,
    val imageWidth: Int = DEFAULTS_IMAGE_WIDTH,
    val imageHeight: Int = DEFAULTS_IMAGE_HEIGHT
)

fun SettingsRaw.toSettings() = Settings(
    serverIpAddress = serverIpAddress,
    serverPort = serverPort,
    maxDetections = maxDetections,
    confidenceThreshold = confidenceThreshold,
    imageWidth = imageWidth,
    imageHeight = imageHeight
)

fun Settings.toSettingsRaw() = SettingsRaw(
    serverIpAddress = serverIpAddress.orEmpty(),
    serverPort = serverPort.orEmpty(),
    maxDetections = maxDetections,
    confidenceThreshold = confidenceThreshold,
    imageWidth = imageWidth,
    imageHeight = imageHeight
)