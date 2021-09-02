package com.example.camera.processing

import android.os.Parcelable
import android.util.Size
import kotlinx.parcelize.Parcelize

@Parcelize
data class Settings(
    var serverIpAddress: String? = null,
    var serverPort: String? = null,
    var localInference: Boolean,
    var maxDetections: Int,
    var confidenceThreshold: Int,
    var imageWidth: Int,
    var imageHeight: Int
) : Parcelable {
    val serverAddressFull: String
        get() = "http://$serverIpAddress:$serverPort/"

    val imageSize: Size
        get() = Size(imageWidth, imageHeight)

    fun toQuery(): String =
        "maxDetections=$maxDetections&confidenceThreshold=$confidenceThreshold&imageWidth=$imageWidth&imageHeight=$imageHeight"
}
