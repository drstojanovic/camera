package com.example.camera.presentation.main

import android.graphics.RectF

class TrackingBox(
    val location: RectF,
    val confidence: Float,
    val title: String,
    val color: Int
) {
    val description: String = "$title %.1f%%".format(confidence * 100.0f)
}
