package com.example.camera.presentation.setup

import android.util.Size

class SetupData {
    val resolutions = listOf(Size(480, 640), Size(300, 400))
    val resolutionsLabels: List<String> get() = resolutions.map { "${it.width}x${it.height}" }

    val detectionCountLimitMin = 1
    val detectionCountLimitMinLabel = "$detectionCountLimitMin"
    val detectionCountLimitMax = 10
    val detectionCountLimitMaxLabel = "$detectionCountLimitMax"

    val confidenceThresholdRangeMin = 30
    val confidenceThresholdRangeMinLabel = "$confidenceThresholdRangeMin"
    val confidenceThresholdRangeMax = 100
    val confidenceThresholdRangeMaxLabel = "$confidenceThresholdRangeMax"
    val confidenceThresholdSliderStep = 10
}
