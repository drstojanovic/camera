package com.example.camera.presentation.classification

import android.graphics.RectF
import com.example.camera.processing.classification.ClassificationResult
import com.example.camera.utils.UNKNOWN_CLASSIFICATION_CONFIDENCE
import com.example.camera.utils.UNKNOWN_CLASSIFICATION_LABEL

class ClassificationResultView(
    val id: String = "",
    val location: RectF,
    val color: Int,
    val resultPrimary: ClassificationView,
    val resultSecondary: ClassificationView?,
    val resultTertiary: ClassificationView?
)

fun ClassificationResult.toClassificationResultView(color: Int) =
    ClassificationResultView(
        id = id,
        location = location,
        color = color,
        resultPrimary = classifications.takeIf { it.isNotEmpty() }?.get(0)?.toClassificationView()
            ?: ClassificationView(UNKNOWN_CLASSIFICATION_CONFIDENCE, UNKNOWN_CLASSIFICATION_LABEL),
        resultSecondary = classifications.takeIf { it.size > 1 }?.get(1)?.toClassificationView(),
        resultTertiary = classifications.takeIf { it.size > 2 }?.get(2)?.toClassificationView()
    )
