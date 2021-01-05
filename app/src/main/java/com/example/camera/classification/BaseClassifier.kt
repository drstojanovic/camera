package com.example.camera.classification

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.min

abstract class BaseClassifier(
    private val context: Context,
    private val numOfThreads: Int,
) {

    companion object {
        private const val MAX_RESULTS = 4
    }

    private var imageSizeX = 0
    private var imageSizeY = 0
    private val gpuDelegate: GpuDelegate? = null
    private val nnApiDelegate: NnApiDelegate? = null

    private val tfLiteOptions = Interpreter.Options()
    private lateinit var tfLite: Interpreter
    private lateinit var labels: List<String>
    private lateinit var inputImageBuffer: TensorImage
    private lateinit var outputProbabilityBuffer: TensorBuffer
    private lateinit var probabilityProcessor: TensorProcessor

    init {
        init()
    }

    private fun init() {
        val tfLiteModel = FileUtil.loadMappedFile(context, provideModelFilePath())
        tfLiteOptions.setNumThreads(numOfThreads)
        tfLite = Interpreter(tfLiteModel, tfLiteOptions)
        labels = FileUtil.loadLabels(context, provideLabelsFilePath())

        val imageTensorIndex = 0
        val imageType = tfLite.getInputTensor(imageTensorIndex).dataType()
        tfLite.getInputTensor(imageTensorIndex).shape().let { imageShape ->
            imageSizeY = imageShape[1]
            imageSizeX = imageShape[2]
        }

        val probabilityTensorIndex = 0
        val probabilityShape = tfLite.getOutputTensor(probabilityTensorIndex).shape()
        val probabilityDataType = tfLite.getOutputTensor(probabilityTensorIndex).dataType()

        inputImageBuffer = TensorImage(imageType)
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
        probabilityProcessor = TensorProcessor.Builder().add(providePostProcessNormalizationOperator()).build()
    }

    fun recognizeImage(bitmap: Bitmap, sensorOrientation: Int): List<Recognition> {
        inputImageBuffer = loadImage(bitmap, sensorOrientation)
        tfLite.run(inputImageBuffer.buffer, outputProbabilityBuffer.buffer.rewind())

        return getTopKPredictions(
            TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer)).mapWithFloatValue
        )
    }

    private fun loadImage(bitmap: Bitmap, sensorOrientation: Int): TensorImage {
        inputImageBuffer.load(bitmap)
        val cropSize = min(bitmap.width, bitmap.height)
        val rotationCount = sensorOrientation / 90

        return ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
            .add(Rot90Op(rotationCount))
            .add(providePreProcessNormalizationOperator())
            .build()
            .process(inputImageBuffer)
    }

    private fun getTopKPredictions(labelProb: Map<String, Float>): List<Recognition> =
        labelProb.entries
            .map { entry -> Recognition(entry.key, entry.key, entry.value, null) }
            .sortedByDescending { it.confidence }
            .take(MAX_RESULTS)

    abstract fun provideModelFilePath(): String
    abstract fun provideLabelsFilePath(): String
    open fun providePostProcessNormalizationOperator() = NormalizeOp(0f, 1f)
    open fun providePreProcessNormalizationOperator() = NormalizeOp(0f, 1f)

}