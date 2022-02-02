package com.example.camera.presentation.detection

import android.graphics.RectF

class TrackingBox(
    val location: RectF,
    val confidence: Float,
    val title: String,
    val color: Int
) {
    val description: String = "$title %.1f%%".format(confidence)
}
