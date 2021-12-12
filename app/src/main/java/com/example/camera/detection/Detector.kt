package com.example.camera.detection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.nio.MappedByteBuffer

class Detector(
    context: Context,
    modelFileName: String,
    private val scoreThreshold: Float,
    private val numberOfThreads: Int = 4
) : IDetector {

    companion object {
        private const val NUMBER_OF_DETECTIONS = 10
    }

    private var modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, modelFileName)
    private var optionsBuilder: ObjectDetector.ObjectDetectorOptions.Builder = getDetectorOptions()
    private var objectDetector: ObjectDetector = initDetector()

    override fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        val startTime = System.currentTimeMillis()
        Log.d("OD - loading time:", (System.currentTimeMillis() - startTime).toString())
        val result = objectDetector.detect(TensorImage.fromBitmap(bitmap))
        Log.d("OD - inference time:", (System.currentTimeMillis() - startTime).toString())
        return result.mapIndexed { index, detection -> detection.toRecognition(index) }
    }

    override fun setNumberOfThreads(numberOfThreads: Int) {
        optionsBuilder.setBaseOptions(BaseOptions.builder().setNumThreads(numberOfThreads).build())
        recreateInterpreter()
    }

    private fun recreateInterpreter() {
        objectDetector.close()
        objectDetector = initDetector()
    }

    private fun initDetector() = ObjectDetector.createFromBufferAndOptions(modelBuffer, optionsBuilder.build())

    private fun getDetectorOptions() = ObjectDetector.ObjectDetectorOptions.builder()
        .setScoreThreshold(scoreThreshold)
        .setBaseOptions(BaseOptions.builder().setNumThreads(numberOfThreads).build())
        .setMaxResults(NUMBER_OF_DETECTIONS)
}
