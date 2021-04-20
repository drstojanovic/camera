package com.example.camera.detection

import android.graphics.RectF

data class Recognition(
    val id: String = "",
    val title: String = "",
    val confidence: Float = 0f,
    val location: RectF
) {

    override fun toString(): String =
        "[$id] $title %.1f%% $location".format(confidence * 100.0f).trim()
}
