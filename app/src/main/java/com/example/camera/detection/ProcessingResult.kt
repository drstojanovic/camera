package com.example.camera.detection

class ProcessingResult(
    val recognitions: List<Recognition>,
    val recognitionTime: Int,
    val imageSizeBytes: Int
) {
    var avgRecognitionTime: Float = 0f
    var avgImageSize: Float = 0f

    val lastImageSizeKbString get() = "%.1f Kb".format(imageSizeBytes / 1024f)
    val avgImageSizeKbString get() = "%.1f Kb".format(avgImageSize / 1024)
    val lastRecognitionTimeString get() = "$recognitionTime ms"
    val avgRecognitionTimeString get() = "%.1f ms".format(avgRecognitionTime)
}
