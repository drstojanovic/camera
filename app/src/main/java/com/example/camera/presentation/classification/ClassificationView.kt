package com.example.camera.presentation.classification

import com.example.camera.processing.classification.Classification

data class ClassificationView(
    val confidence: Float,
    val title: String
) {
    val confidenceString get() = "%.1f".format(confidence) + "%"
}

fun Classification.toClassificationView() = ClassificationView(
    confidence = confidence,
    title = title
)
