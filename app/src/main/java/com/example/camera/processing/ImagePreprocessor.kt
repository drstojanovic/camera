package com.example.camera.processing

import android.graphics.Bitmap
import android.util.Base64
import com.example.camera.utils.ImageUtils
import kotlin.math.abs

object ImagePreprocessor {

    fun setSizeAndOrientation(image: Bitmap, orientation: Int, width: Int, height: Int): Bitmap {
        val srcWidth = if (abs(orientation) in setOf(90, 270)) image.height else image.width
        val srcHeight = if (abs(orientation) in setOf(90, 270)) image.width else image.height
        return Bitmap.createBitmap(
            image, 0, 0, image.width, image.height,
            ImageUtils.getTransformationMatrix(
                srcWidth, srcHeight,
                width, height,
                -orientation
            ), true
        )
    }

    fun encodeBytes(imageBytes: ByteArray): ByteArray =
        Base64.encode(imageBytes, Base64.NO_WRAP)
}
