package com.example.camera.processing.detection

import android.graphics.RectF
import com.example.camera.presentation.classification.ClassificationResultView
import org.tensorflow.lite.task.vision.detector.Detection

data class Recognition(
    val id: String = "",
    val title: String = "",
    val confidence: Float = 0f,
    val location: RectF
) {

    override fun toString(): String =
        "[$id] $title %.1f%% $location".format(confidence).trim()

    fun toShortString(): String =
        if (confidence != 0f) {
            "$id. $title %.1f%%".format(confidence).trim()
        } else {
            "$id. $title".trim()
        }
}

fun Detection.toRecognition(index: Int, width: Float, height: Float) = Recognition(
    id = (index + 1).toString(),
    title = categories[0].label,
    confidence = categories[0].score * 100,
    location = RectF(
        if (boundingBox.left > 0) boundingBox.left else 0f,
        if (boundingBox.top > 0) boundingBox.top else 0f,
        if (boundingBox.right < width) boundingBox.right else width,
        if (boundingBox.bottom < height) boundingBox.bottom else height
    )
)

fun RecognitionRaw.toRecognition(index: Int) = Recognition(
    id = (index + 1).toString(),
    title = title,
    confidence = confidence,
    location = RectF(location[0], location[1], location[2], location[3])
)

fun ClassificationResultView.toRecognition() = Recognition(
    id = id,
    title = resultPrimary.title,
    confidence = resultPrimary.confidence,
    location = location
)
