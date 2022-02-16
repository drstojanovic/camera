package com.example.camera.processing.classification

import android.graphics.RectF

class ClassificationResult(
    val id: String = "",
    val classifications: List<Classification>,
    val location: RectF
)
