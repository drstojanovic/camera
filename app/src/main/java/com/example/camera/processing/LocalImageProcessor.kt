package com.example.camera.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.camera.detection.ObjectDetector
import com.example.camera.detection.ProcessingResult
import com.example.camera.detection.Recognition
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream

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
        private const val LABELS_FILE = "labels.txt"
    }

    private val detector = ObjectDetector(
        context,
        modelFileName = MODEL_FILE,
        labelFileName = LABELS_FILE,
        scoreThreshold = settings.confidenceThreshold / 100f
    )

    //todo: add option to completely disable the compression process; compare speed with and without compression
    override fun process(image: Bitmap): Single<ProcessingResult> =
        Single.fromCallable {
            val byteArray = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, settings.imageQuality, byteArray)
            val bytes = byteArray.toByteArray()
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
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    private fun filterInvalidDetections(list: List<Recognition>, width: Int, height: Int): List<Recognition> =
        list.filterNot { it.location.width() > width || it.location.height() > height }
            .take(settings.maxDetections)
}
