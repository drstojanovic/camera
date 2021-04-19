package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import com.example.camera.classification.Recognition
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
        private val INPUT_SIZE = Size(640, 480)
    }

    private val detector = ObjectDetector(context, MAX_DETECTIONS, MODEL_FILE, LABELS_FILE, INPUT_SIZE)

    fun processImage(image: Bitmap, orientation: Int): Single<List<Recognition>> =
        Single.fromCallable { detector.recognizeImage(preprocessImage(image, orientation)) }
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
}
