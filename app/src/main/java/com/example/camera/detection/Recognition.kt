package com.example.camera.detection

import android.graphics.RectF
import org.tensorflow.lite.task.vision.detector.Detection

data class Recognition(
    val id: String = "",
    val title: String = "",
    val confidence: Float = 0f,
    val location: RectF
) {

    override fun toString(): String =
        "[$id] $title %.1f%% $location".format(confidence * 100.0f).trim()

    fun toShortString(): String =
        "$id. $title %.1f%%".format(confidence * 100.0f).trim()
}

fun Detection.toRecognition(index: Int) = Recognition(
    id = (index + 1).toString(),
    title = categories[0].label,
    confidence = categories[0].score,
    location = boundingBox
)