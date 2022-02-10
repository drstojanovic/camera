package com.example.camera.processing.detection

import android.graphics.RectF
import com.example.camera.presentation.classification.ClassificationResultView
import com.example.camera.utils.UNKNOWN_CLASSIFICATION_CONFIDENCE
import com.example.camera.utils.UNKNOWN_CLASSIFICATION_LABEL
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

fun Detection.toRecognition(index: Int) = Recognition(
    id = (index + 1).toString(),
    title = categories[0].label,
    confidence = categories[0].score * 100,
    location = boundingBox
)

fun RecognitionRaw.toRecognition(index: Int) = Recognition(
    id = (index + 1).toString(),
    title = title,
    confidence = confidence,
    location = RectF(location[0], location[1], location[2], location[3])
)

fun ClassificationResultView.toRecognition() = Recognition(
    id = id,
    title = resultPrimary?.title ?: UNKNOWN_CLASSIFICATION_LABEL,
    confidence = resultPrimary?.confidence ?: UNKNOWN_CLASSIFICATION_CONFIDENCE,
    location = location
)
