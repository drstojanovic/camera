package com.example.camera.classification

import android.content.Context

class ClassifierQuantizedMobileNetV2(
    context: Context,
    numberOfThreads: Int
) : BaseClassifier(context, numberOfThreads) {

    override fun provideModelFilePath() = "classification/classification_quantized_model.tflite"

    override fun provideLabelsFilePath() = "classification/labels.txt"

}