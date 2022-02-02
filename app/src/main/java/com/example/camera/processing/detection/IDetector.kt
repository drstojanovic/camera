package com.example.camera.processing.detection

import android.graphics.Bitmap

interface IDetector {

    fun recognizeImage(bitmap: Bitmap): List<Recognition>

    fun setNumberOfThreads(numberOfThreads: Int)
}
