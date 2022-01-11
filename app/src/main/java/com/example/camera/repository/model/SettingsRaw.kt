package com.example.camera.repository.model

import com.example.camera.processing.Settings
import com.example.camera.utils.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SettingsRaw(
    val serverIpAddress: String? = null,
    val serverPort: String? = null,
    val threadCount: Int? = DEFAULT_THREAD_COUNT,
    val localInference: Boolean = DEFAULTS_LOCAL_INFERENCE,
    val maxDetections: Int = DEFAULTS_MAX_DETECTIONS,
    val confidenceThreshold: Int = DEFAULTS_CONFIDENCE_THRESHOLD,
    val imageQuality: Int = DEFAULTS_IMAGE_QUALITY,
    val imageWidth: Int = DEFAULTS_IMAGE_WIDTH,
    val imageHeight: Int = DEFAULTS_IMAGE_HEIGHT
)

fun SettingsRaw.toSettings() = Settings(
    serverIpAddress = serverIpAddress,
    serverPort = serverPort,
    threadCount = threadCount,
    localInference = localInference,
    maxDetections = maxDetections,
    confidenceThreshold = confidenceThreshold,
    imageQuality = imageQuality,
    imageWidth = imageWidth,
    imageHeight = imageHeight
)

fun Settings.toSettingsRaw() = SettingsRaw(
    serverIpAddress = serverIpAddress.orEmpty(),
    serverPort = serverPort.orEmpty(),
    threadCount = threadCount,
    localInference = localInference,
    maxDetections = maxDetections,
    confidenceThreshold = confidenceThreshold,
    imageQuality = imageQuality,
    imageWidth = imageWidth,
    imageHeight = imageHeight
)
