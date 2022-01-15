package com.example.camera.detection

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RecognitionRaw(
    val title: String,
    val confidence: Float,
    val location: List<Float>
)
