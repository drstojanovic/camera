package com.example.camera.detection

import android.graphics.RectF
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RecognitionRaw(
    val title: String,
    val confidence: Float,
    val location: List<Float>
)

fun RecognitionRaw.toRecognition(id: Int) = Recognition(
    id = id.toString(),
    title = title,
    confidence = confidence,
    location = RectF(location[0], location[1], location[2], location[3])
)
