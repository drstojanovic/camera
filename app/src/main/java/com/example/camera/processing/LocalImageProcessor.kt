package com.example.camera.processing

import android.content.Context
import android.graphics.Bitmap
import com.example.camera.detection.ObjectDetector
import com.example.camera.detection.Recognition
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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
