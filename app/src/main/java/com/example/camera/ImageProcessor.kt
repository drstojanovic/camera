package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import com.example.camera.detection.Recognition
import com.example.camera.detection.ObjectDetector
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ImageProcessor(
    context: Context
) {

    companion object {
        private const val MODEL_FILE = "detection/lite_model.tflite"
        private const val QUANITIZED_MODEL_FILE = "detection/lite_quanitized_model.tflite"
        private const val LABELS_FILE = "labels.txt"
        private const val MAX_DETECTIONS = 10
        private const val SCORE_THRESHOLD = 0.5f
    }

    private val detector = ObjectDetector(context, MODEL_FILE, LABELS_FILE, MAX_DETECTIONS, SCORE_THRESHOLD)

    fun processImage(image: Bitmap, orientation: Int): Single<List<Recognition>> =
        Single.fromCallable { detector.recognizeImage(preprocessImage(image, orientation)) }
            .flatMap { filterInvalidDetections(it, image.width, image.height) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    private fun preprocessImage(image: Bitmap, orientation: Int): Bitmap =
        image.takeIf { orientation != 0 }
            ?.let {
                Bitmap.createBitmap(
                    image, 0, 0, image.width, image.height,
                    Matrix().apply { postRotate(-orientation.toFloat(), -image.width / 2f, -image.height / 2f) },
                    true
                )
            } ?: image

    private fun filterInvalidDetections(list: List<Recognition>, width: Int, height: Int) =
        Single.fromCallable { list.filterNot { it.location.width() > width || it.location.height() > height } }
}
