package com.example.camera.processing

import android.content.Context
import android.graphics.Bitmap
import com.example.camera.detection.ObjectDetector
import com.example.camera.detection.Recognition
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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
        private const val MODEL_FILE = "detection/lite_model_v7.tflite"
        private const val LABELS_FILE = "labels.txt"
    }

    private val detector = ObjectDetector(
        context,
        modelFileName = MODEL_FILE,
        labelFileName = LABELS_FILE,
        scoreThreshold = settings.confidenceThreshold / 100f
    )

    override fun process(image: Bitmap): Single<List<Recognition>> =
        Single.fromCallable { detector.recognizeImage(image) }
            .flatMap { filterInvalidDetections(it, image.width, image.height) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    private fun filterInvalidDetections(list: List<Recognition>, width: Int, height: Int): Single<List<Recognition>> =
        Single.fromCallable {
            list.filterNot { it.location.width() > width || it.location.height() > height }
                .take(settings.maxDetections)
        }
}
