package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import com.example.camera.tflite.Classifier
import com.example.camera.tflite.ClassifierFloatMobileNet
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ImageProcessor(context: Context) {

    private val classifier = ClassifierFloatMobileNet(context, Classifier.Device.CPU, 4)

    fun processImage(image: Bitmap, orientation: Int): Single<MutableList<Classifier.Recognition>> =
        Single.fromCallable { classifier.recognizeImage(image, orientation) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())

}