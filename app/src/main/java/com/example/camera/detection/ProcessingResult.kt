package com.example.camera.detection

class ProcessingResult(
    val recognitions: List<Recognition>,
    val recognitionTime: Int,
    val imageSizeBytes: Int
) {
    var avgRecognitionTime: Int = 0
    var avgImageSize: Int = 0

    val lastImageSizeBytesString get() = "${imageSizeBytes / 1024} Kb"
    val avgImageSizeKbString get() = "${avgImageSize / 1024} Kb"
    val lastRecognitionTimeString get() = "$recognitionTime ms"
    val avgRecognitionTimeString get() = "$avgRecognitionTime ms"
}
