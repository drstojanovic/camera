package com.example.camera.processing.classification

import android.graphics.Bitmap
import com.example.camera.processing.ImagePreprocessor
import com.example.camera.processing.Settings
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

abstract class MultipleObjectClassifier(protected val settings: Settings) {

    open fun prepareImage(image: Bitmap, orientation: Int) =
        ImagePreprocessor.setSizeAndOrientation(image, orientation, settings.imageWidth, settings.imageHeight)

    fun processImage(image: Bitmap, orientation: Int): Single<List<ClassificationResult>> =
        Single.fromCallable { prepareImage(image, orientation) }
            .flatMap { process(it) }
            .subscribeOn(Schedulers.io())

    protected abstract fun process(image: Bitmap): Single<List<ClassificationResult>>

    abstract fun dispose()
}
