package com.example.camera.classification

import android.content.Context
import org.tensorflow.lite.support.common.ops.NormalizeOp

class ClassifierFloatMobileNetV2(context: Context, numberOfThreads:Int):BaseClassifier(context, numberOfThreads) {

    override fun provideModelFilePath() = "classification/classification_model.tflite"

    override fun provideLabelsFilePath() = "classification/labels.txt"

    override fun providePostProcessNormalizationOperator() = NormalizeOp(0f,1f)

    override fun providePreProcessNormalizationOperator()=NormalizeOp(0f,1f)

}