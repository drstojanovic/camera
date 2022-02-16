package com.example.camera.processing.classification

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClassificationRaw(
    val title: String = "",
    val confidence: Float = 0f,
)

fun ClassificationRaw.toClassification() =
    Classification(
        title = title,
        confidence = confidence
    )
