package com.example.camera.processing

import android.os.Parcelable
import android.util.Size
import kotlinx.parcelize.Parcelize

@Parcelize
data class Settings(
    var serverIpAddress: String? = null,
    var serverPort: String? = null,
    var threadCount: Int?,
    var localInference: Boolean,
    var maxDetections: Int,
    var detectionThreshold: Int,
    var classificationThreshold: Int,
    var imageQuality: Int,
    var imageWidth: Int,
    var imageHeight: Int
) : Parcelable {
    val serverAddressFull: String
        get() = "http://$serverIpAddress:$serverPort/"

    val imageSize: Size
        get() = Size(imageWidth, imageHeight)

    fun toQuery(): String =
        "maxDetections=$maxDetections&detectionThreshold=$detectionThreshold&classificationThreshold=$classificationThreshold"
}
