package com.example.camera.tracking

import android.graphics.RectF

class TrackingBox(
    val location: RectF,
    val confidence: Float,
    val title: String,
    val color: Int
)
