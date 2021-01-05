package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import com.example.camera.classification.ClassifierQuantizedMobileNetV2
import com.example.camera.classification.Recognition
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ImageProcessor(context: Context) {

    private val classifier = ClassifierQuantizedMobileNetV2(context, 4)

    fun processImage(image: Bitmap, orientation: Int): Single<List<Recognition>> =
        Single.fromCallable { classifier.recognizeImage(image, orientation) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

}