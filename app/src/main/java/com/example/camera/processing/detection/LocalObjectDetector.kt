package com.example.camera.processing.detection

import android.content.Context
import android.graphics.BitmapFactory
import com.example.camera.processing.Settings
import com.example.camera.utils.DEFAULT_THREAD_COUNT

/**
 * For local image processing, max detection limit is handled manually (by removing the items), after 10 results are returned from model.
 * This is done because of the static nature of tflite model where output count can not be changed once model is generated.
 * While generating, there is a parameter 'max_detections' which is 10 by default.
 */
class LocalObjectDetector(
    context: Context,
    settings: Settings
) : ObjectDetector(settings) {

    companion object {
        private const val MODEL_FILE = "detection/lite_model.tflite"
    }

    private val detector = Detector(
        context,
        modelFileName = MODEL_FILE,
        scoreThreshold = settings.detectionThreshold / 100f,
        numberOfThreads = settings.threadCount ?: DEFAULT_THREAD_COUNT
    )

    override suspend fun detectObjects(imageBytes: ByteArray): List<Recognition> =
        detector.recognizeImage(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
            .filterInvalidDetections()

    private fun List<Recognition>.filterInvalidDetections(): List<Recognition> =
        filterNot { it.location.width() > settings.imageWidth || it.location.height() > settings.imageHeight }
            .take(settings.maxDetections)
}
