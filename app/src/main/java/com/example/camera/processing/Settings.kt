package com.example.camera.processing

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

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

    fun toQuery(): String =
        "maxDetections=$maxDetections&confidenceThreshold=$confidenceThreshold&imageWidth=$imageWidth&imageHeight=$imageHeight"
}
