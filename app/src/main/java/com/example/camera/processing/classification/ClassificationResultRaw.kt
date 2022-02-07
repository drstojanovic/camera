package com.example.camera.processing.classification

import android.graphics.RectF
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ClassificationResultRaw(
    val classifications: List<ClassificationRaw>,
)

fun ClassificationResultRaw.toClassificationResult(index: Int, location: RectF) =
    ClassificationResult(
        id = (index + 1).toString(),
        classifications = classifications.map { it.toClassification() },
        location = location
    )
