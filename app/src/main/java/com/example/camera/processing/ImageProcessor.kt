package com.example.camera.processing

import android.graphics.Bitmap
import com.example.camera.detection.Recognition
import com.example.camera.utils.ImageUtils
import io.reactivex.Single
import kotlin.math.abs

const val TAG = "ImageProcessor"

abstract class ImageProcessor(protected val settings: Settings) {

    val selectedInputSize get() = settings.imageSize

    fun processImage(image: Bitmap, orientation: Int): Single<List<Recognition>> {
        return process(preprocessImage(image, orientation))
    }

    abstract fun process(image: Bitmap): Single<List<Recognition>>

    open fun dispose() {}

    protected open fun preprocessImage(image: Bitmap, orientation: Int): Bitmap {
        val srcWidth = if (abs(orientation) in setOf(90, 270)) image.height else image.width
        val srcHeight = if (abs(orientation) in setOf(90, 270)) image.width else image.height
        return Bitmap.createBitmap(
            image, 0, 0, image.width, image.height,
            ImageUtils.getTransformationMatrix(
                srcWidth, srcHeight,
                settings.imageSize.width, settings.imageSize.height,
                -orientation
            ), true
        )
    }

    protected fun filterInvalidDetections(list: List<Recognition>, width: Int, height: Int): Single<List<Recognition>> =
        Single.fromCallable { list.filterNot { it.location.width() > width || it.location.height() > height } }
}
