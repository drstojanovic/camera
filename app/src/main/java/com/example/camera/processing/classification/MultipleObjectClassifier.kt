package com.example.camera.processing.classification

import android.graphics.Bitmap
import com.example.camera.processing.ImagePreprocessor
import com.example.camera.processing.Settings
import com.example.camera.utils.tryToExecute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class MultipleObjectClassifier(val settings: Settings) {

    open suspend fun prepareImage(image: Bitmap, orientation: Int) =
        ImagePreprocessor.setSizeAndOrientation(image, orientation, settings.imageWidth, settings.imageHeight)

    suspend fun processImage(image: Bitmap, orientation: Int): Result<List<ClassificationResult>> =
        withContext(Dispatchers.Default) {
            tryToExecute {
                process(prepareImage(image, orientation))
            }
        }

    protected abstract suspend fun process(image: Bitmap): List<ClassificationResult>

    abstract fun dispose()
}
