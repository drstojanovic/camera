package com.example.camera.presentation.detection

import android.graphics.RectF

class TrackingBox(
    val location: RectF,
    val color: Int,
    confidence: Float,
    title: String
) {
    val description: String =
        if (confidence != 0f) {
            "$title %.1f%%".format(confidence).trim()
        } else {
            title.trim()
        }
}
