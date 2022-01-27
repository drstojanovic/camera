package com.example.camera.detection

import android.content.Context
import android.graphics.Bitmap
import com.example.camera.utils.DEFAULTS_MAX_DETECTIONS
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

    private var modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, modelFileName)
    private var optionsBuilder: ObjectDetector.ObjectDetectorOptions.Builder = getDetectorOptions()
    private var objectDetector: ObjectDetector = initDetector()

    override fun recognizeImage(bitmap: Bitmap): List<Recognition> =
        objectDetector.detect(TensorImage.fromBitmap(bitmap))
            .mapIndexed { index, detection -> detection.toRecognition(index) }

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
        .setMaxResults(DEFAULTS_MAX_DETECTIONS)
}
