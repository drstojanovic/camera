package com.example.camera.processing

class Settings(
    private val serverIpAddress: String,
    private val serverPort: String,
    private val maxDetections: Int = 10,
    private val confidenceThreshold: Int = 50,
    private val imageWidth: Int = 480,
    private val imageHeight: Int = 640
) {
    val serverAddress: String
        get() = "http://$serverIpAddress:$serverPort/"

    fun toQuery(): String =
        "maxDetections=$maxDetections&confidenceThreshold=$confidenceThreshold&imageWidth=$imageWidth&imageHeight=$imageHeight"

}
