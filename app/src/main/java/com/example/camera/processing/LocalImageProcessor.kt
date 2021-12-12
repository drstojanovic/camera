package com.example.camera.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.camera.detection.Detector
import com.example.camera.detection.ProcessingResult
import com.example.camera.detection.Recognition
import com.example.camera.utils.ImageUtils.getByteArray
import io.reactivex.Single

/**
 * For local image processing, max detection limit is handled manually (by removing the items), after 10 results are returned from model.
 * This is done because of the static nature of tflite model where output count can not be changed once model is generated.
 * While generating, there is a parameter 'max_detections' which is 10 by default.
 */
class LocalImageProcessor(
    context: Context,
    settings: Settings
) : ImageProcessor(settings) {

    companion object {
        private const val MODEL_FILE = "detection/lite_model.tflite"
    }

    private val detector = Detector(
        context,
        modelFileName = MODEL_FILE,
        scoreThreshold = settings.confidenceThreshold / 100f
    )

    override fun process(image: Bitmap): Single<ProcessingResult> =
        Single.fromCallable {
            val bytes = image.getByteArray(settings.imageQuality)
            val processingStart = System.currentTimeMillis()
            ProcessingResult(
                recognitions = filterInvalidDetections(
                    detector.recognizeImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)),
                    image.width,
                    image.height
                ),
                recognitionTime = (System.currentTimeMillis() - processingStart).toInt(),
                imageSizeBytes = bytes.size
            )
        }

    private fun filterInvalidDetections(list: List<Recognition>, width: Int, height: Int): List<Recognition> =
        list.filterNot { it.location.width() > width || it.location.height() > height }
            .take(settings.maxDetections)
}
