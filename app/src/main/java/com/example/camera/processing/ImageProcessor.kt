package com.example.camera.processing

import android.graphics.Bitmap
import com.example.camera.detection.ProcessingResult
import com.example.camera.utils.ImageUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs

const val TAG = "ImageProcessor"

abstract class ImageProcessor(val settings: Settings) {

    private var sampleCount = 0
    private var avgImageSize = 0f
    private var avgRecognitionTime = 0f

    fun processImage(image: Bitmap, orientation: Int): Single<ProcessingResult> {
        return process(preprocessImage(image, orientation))
            .map { calculateStats(it) }
            .subscribeOn(Schedulers.computation())
    }

    open fun dispose() {}

    protected abstract fun process(image: Bitmap): Single<ProcessingResult>

    protected open fun preprocessImage(image: Bitmap, orientation: Int): Bitmap {
        val srcWidth = if (abs(orientation) in setOf(90, 270)) image.height else image.width
        val srcHeight = if (abs(orientation) in setOf(90, 270)) image.width else image.height
        return Bitmap.createBitmap(
            image, 0, 0, image.width, image.height,
            ImageUtils.getTransformationMatrix(
                srcWidth, srcHeight,
                settings.imageWidth, settings.imageHeight,
                -orientation
            ), true
        )
    }

    private fun calculateStats(result: ProcessingResult): ProcessingResult {
        avgImageSize = (avgImageSize * sampleCount + result.imageSizeBytes) / (sampleCount + 1)
        avgRecognitionTime = (avgRecognitionTime * sampleCount + result.recognitionTime) / (sampleCount + 1)
        sampleCount++

        return result.apply {
            avgImageSize = this@ImageProcessor.avgImageSize
            avgRecognitionTime = this@ImageProcessor.avgRecognitionTime
        }
    }
}
