package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import com.example.camera.detection.ObjectDetector
import com.example.camera.detection.Recognition
import com.example.camera.utils.ImageUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs

class ImageProcessor(
    context: Context
) {

    companion object {
        private const val MODEL_FILE = "detection/lite_model_v7.tflite"
        private const val MODEL_FILE_V3 = "detection/model_640x480.tflite"
        private const val QUANITIZED_MODEL_FILE = "detection/quanitized_v7.tflite"
        private const val QUANITIZED_640x480_MODEL_FILE = "detection/quanitized_640x480.tflite"
        private const val MODEL_640x480_FILE = "detection/model640x480.tflite"
        private const val LABELS_FILE = "labels.txt"
        private const val MAX_DETECTIONS = 10
        private const val SCORE_THRESHOLD = 0.5f

//      val inputSize = Size(300, 400)
        val inputSize = Size(480, 640)
    }

    private val detector = ObjectDetector(context, MODEL_FILE, LABELS_FILE, MAX_DETECTIONS, SCORE_THRESHOLD)

    fun processImage(image: Bitmap, orientation: Int): Single<List<Recognition>> =
        Single.fromCallable { detector.recognizeImage(preprocessImage(image, orientation)) }
            .flatMap { filterInvalidDetections(it, image.width, image.height) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

    private fun preprocessImage(image: Bitmap, orientation: Int): Bitmap {
        val srcWidth = if (abs(orientation) in setOf(90, 270)) image.height else image.width
        val srcHeight = if (abs(orientation) in setOf(90, 270)) image.width else image.height
        return Bitmap.createBitmap(
            image, 0, 0, image.width, image.height,
            ImageUtils.getTransformationMatrix(srcWidth, srcHeight, inputSize.width, inputSize.height, -orientation),
            true
        )
    }

    private fun filterInvalidDetections(list: List<Recognition>, width: Int, height: Int) =
        Single.fromCallable { list.filterNot { it.location.width() > width || it.location.height() > height } }
}
