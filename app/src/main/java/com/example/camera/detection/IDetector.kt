package com.example.camera.detection

import android.graphics.Bitmap
import com.example.camera.classification.Recognition

interface IDetector {

    fun recognizeImage(bitmap: Bitmap): List<Recognition>

    fun setNumberOfThreads(numberOfThreads: Int)
}
