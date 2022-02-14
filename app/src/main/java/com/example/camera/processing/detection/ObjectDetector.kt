package com.example.camera.processing.detection

import android.graphics.Bitmap
import com.example.camera.processing.ImagePreprocessor
import com.example.camera.processing.Settings
import com.example.camera.utils.ImageUtils.getByteArray
import com.example.camera.utils.tryToExecute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val TAG = "ImageProcessor"
const val MAX_PROCESSING_TIME_MILLIS = 5000L

abstract class ObjectDetector(val settings: Settings) {

    private var sampleCount = 0
    private var avgImageSize = 0f
    private var avgRecognitionTime = 0f

    /**
     * Called for raw (non-preprocessed) bitmap just taken from camera.
     * Here, before detection itself, bitmap is properly resized and rotated.
     */
    suspend fun processImage(image: Bitmap, orientation: Int): Result<DetectionResult> =
        withContext(Dispatchers.Default) {
            var bytes: ByteArray
            val processingStart = System.currentTimeMillis()

            tryToExecute {
                preprocessImage(image, orientation)
                    .let { bitmap -> bitmap.getByteArray(imageQuality = settings.imageQuality).also { bytes = it } }
                    .let { imageBytes -> detectObjects(imageBytes) }
                    .let { generateProcessingResult(it, bytes.size, processingStart) }
            }
        }

    /**
     * Called for image bytes got from bitmap.
     * Here, it is considered that image(bytes) is previously preprocessed (orientation, size).
     */
    abstract suspend fun detectObjects(imageBytes: ByteArray): List<Recognition>

    open fun dispose() {}

    protected open fun preprocessImage(image: Bitmap, orientation: Int): Bitmap =
        ImagePreprocessor.setSizeAndOrientation(image, orientation, settings.imageWidth, settings.imageHeight)

    private fun generateProcessingResult(
        recognitions: List<Recognition>,
        imageSizeBytes: Int,
        processingStart: Long
    ): DetectionResult {
        val recognitionTime = (System.currentTimeMillis() - processingStart).toInt()
        avgImageSize = (avgImageSize * sampleCount + imageSizeBytes) / (sampleCount + 1)
        avgRecognitionTime = (avgRecognitionTime * sampleCount + recognitionTime) / (sampleCount + 1)
        sampleCount++

        return DetectionResult(
            recognitions = recognitions,
            recognitionTime = recognitionTime,
            imageSizeBytes = imageSizeBytes,
            averageImageSize = this@ObjectDetector.avgImageSize,
            averageRecognitionTime = this@ObjectDetector.avgRecognitionTime
        )
    }
}
