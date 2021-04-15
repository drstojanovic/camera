package com.example.camera.classification

import android.graphics.RectF

class Recognition(
    val id: String = "",
    val title: String = "",
    val confidence: Float = 0f,
    val location: RectF? = null
) {

    override fun toString(): String =
        "[$id] $title " + String.format("(%.1f%%) ", confidence * 100.0f).let { result ->
            if (location != null) "$result $location "
            else result
        }.trim()
}
