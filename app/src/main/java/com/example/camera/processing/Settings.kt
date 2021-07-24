package com.example.camera.processing

class Settings(
   var serverIpAddress: String? = null,
   var serverPort: String? = null,
   var maxDetections: Int = 10,
   var confidenceThreshold: Int = 50,
   var imageWidth: Int = 480,
   var imageHeight: Int = 640
) {
    val serverAddress: String
        get() = "http://$serverIpAddress:$serverPort/"

    fun toQuery(): String =
        "maxDetections=$maxDetections&confidenceThreshold=$confidenceThreshold&imageWidth=$imageWidth&imageHeight=$imageHeight"

}
