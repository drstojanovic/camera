package com.example.camera.detection

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import com.example.camera.classification.Recognition
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.charset.Charset

class ObjectDetector(
    context: Context,
    maxDetections: Int,
    modelFileName: String,
    labelFileName: String,
    private val inputSize: Size,
    private val numberOfThreads: Int = 4
) : IDetector {

    private lateinit var tfLite: Interpreter
    private lateinit var tfLiteOptions: Interpreter.Options
    private lateinit var tfLiteModel: MappedByteBuffer
    private val labels = arrayListOf<String>()
    private val intValues = IntArray(inputSize.width * inputSize.height)
    private val imageData = ByteBuffer.allocateDirect(inputSize.width * inputSize.height * 3)
    private val detectionResult = DetectionResult(maxDetections)

    init {
        val modelFile = ModelUtils.loadModelFile(context.assets, modelFileName)
        val metadata = MetadataExtractor(modelFile)

        initLabels(metadata, labelFileName)
        initInterpreter(modelFile)
    }

    override fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        populateImageByteArray()
        tfLite.runForMultipleInputsOutputs(Array<Any>(1) { imageData }, detectionResult.valuesMap)
        return detectionResult.getRecognitions(labels, inputSize)
    }

    override fun setNumberOfThreads(numberOfThreads: Int) {
        tfLiteOptions.setNumThreads(numberOfThreads)
        recreateInterpreter()
    }

    private fun initLabels(metadata: MetadataExtractor, labelFileName: String) =
        metadata.getAssociatedFile(labelFileName)
            .bufferedReader(Charset.defaultCharset())
            .useLines { labels.addAll(it) }

    private fun initInterpreter(modelFile: MappedByteBuffer) {
        tfLiteModel = modelFile
        tfLiteOptions = Interpreter.Options().apply { setNumThreads(numberOfThreads) }
        tfLite = Interpreter(tfLiteModel, tfLiteOptions)
    }

    private fun recreateInterpreter() {
        tfLite.close()
        tfLite = Interpreter(tfLiteModel, tfLiteOptions)
    }

    private fun populateImageByteArray() {
        imageData.rewind()
        for (row in 0 until inputSize.height) {
            for (col in 0 until inputSize.width) {
                intValues[row * inputSize.width + col].let { pixel ->
                    imageData.put(((pixel shr 16) and 0xFF).toByte())
                    imageData.put(((pixel shr 8) and 0xFF).toByte())
                    imageData.put((pixel and 0xFF).toByte())
                }
            }
        }
    }
}
