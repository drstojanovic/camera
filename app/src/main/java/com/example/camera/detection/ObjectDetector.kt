package com.example.camera.detection

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import com.example.camera.classification.Recognition
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.metadata.MetadataExtractor
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

    companion object {
        private const val FLOAT_MODEL_IMAGE_MEAN = 127.5f
        private const val FLOAT_MODEL_IMAGE_STD = 127.5f
    }

    private lateinit var tfLite: Interpreter
    private lateinit var tfLiteOptions: Interpreter.Options
    private lateinit var tfLiteModel: MappedByteBuffer
    private lateinit var inputTensorImage: TensorImage
    private val labels = arrayListOf<String>()
    private val detectionResult = DetectionResult(maxDetections)

    init {
        val modelFile = ModelUtils.loadModelFile(context.assets, modelFileName)
        val metadata = MetadataExtractor(modelFile)

        initLabels(metadata, labelFileName)
        initInterpreter(modelFile)
    }

    override fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        inputTensorImage = loadTensorImage(bitmap)
        tfLite.runForMultipleInputsOutputs(Array<Any>(1) { inputTensorImage.buffer }, detectionResult.valuesMap)
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

    private fun loadTensorImage(bitmap: Bitmap): TensorImage =
        TensorImage(tfLite.getInputTensor(0).dataType())
            .apply { load(bitmap) }
            .let {
                ImageProcessor.Builder()
                    .add(NormalizeOp(FLOAT_MODEL_IMAGE_MEAN, FLOAT_MODEL_IMAGE_STD))
                    .build()
                    .process(it)
            }
}
