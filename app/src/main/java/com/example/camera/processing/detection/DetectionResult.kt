package com.example.camera.processing.detection

class DetectionResult(
    val recognitions: List<Recognition>,
    val recognitionTime: Int,
    val imageSizeBytes: Int,
    val averageRecognitionTime: Float = 0f,
    val averageImageSize: Float = 0f
) {
    val lastImageSizeKbString get() = "%.1f Kb".format(imageSizeBytes / 1024f)
    val avgImageSizeKbString get() = "%.1f Kb".format(averageImageSize / 1024)
    val lastRecognitionTimeString get() = "$recognitionTime ms"
    val avgRecognitionTimeString get() = "%.1f ms".format(averageRecognitionTime)
}
