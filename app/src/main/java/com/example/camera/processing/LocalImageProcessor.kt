package com.example.camera.processing

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import com.example.camera.detection.ObjectDetector
import com.example.camera.detection.Recognition
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LocalImageProcessor(
    context: Context,
    settings: Settings
) : ImageProcessor() {

    companion object {
        private const val MODEL_FILE = "detection/lite_model_v7.tflite"
        private const val MODEL_FILE_V3 = "detection/model_640x480.tflite"
        private const val QUANITIZED_MODEL_FILE = "detection/quanitized_v7.tflite"
        private const val QUANITIZED_640x480_MODEL_FILE = "detection/quanitized_640x480.tflite"
        private const val MODEL_640x480_FILE = "detection/model640x480.tflite"
        private const val LABELS_FILE = "labels.txt"
        private const val MAX_DETECTIONS = 10
        private const val SCORE_THRESHOLD = 0.5f
        val inputSize = Size(480, 640) // TODO: remove it from static field and check usages
    }

    private val detector = ObjectDetector(
        context,
        modelFileName = MODEL_FILE,     // TODO: use file depending on image size
        labelFileName = LABELS_FILE,
        maxDetections = settings.maxDetections,
        scoreThreshold = settings.confidenceThreshold / 100f
    )

    override fun process(image: Bitmap): Single<List<Recognition>> =
        Single.fromCallable { detector.recognizeImage(image) }
            .flatMap { filterInvalidDetections(it, image.width, image.height) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
}
