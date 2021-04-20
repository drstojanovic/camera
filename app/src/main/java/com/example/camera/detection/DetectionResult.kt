package com.example.camera.detection

import android.graphics.RectF
import android.util.Size
import com.example.camera.classification.Recognition
import kotlin.math.min

/**
 * Model returns map of arrays. When processing result, we always flat that first array.
 * For example, for key of 0 (location) we get this result: [ [ [1, 5, 6, 5], [22, 16, 19, 8], [12, 26, 39, 18] ] ]
 * To get bbox location for detection number 2, first we flatten the result - result[0], and then get by index 2 - result[0][2]
 */
class DetectionResult(private val maxNumberOfDetections: Int) {

    companion object {
        private const val LOCATIONS_INDEX = 0
        private const val CLASSES_INDEX = 1
        private const val SCORES_INDEX = 2
        private const val COUNT_INDEX = 3
    }

    val valuesMap = mutableMapOf<Int, Any>().apply {
        put(LOCATIONS_INDEX, Array(1) { Array(maxNumberOfDetections) { FloatArray(4) } })
        put(CLASSES_INDEX, Array(1) { FloatArray(maxNumberOfDetections) })
        put(SCORES_INDEX, Array(1) { FloatArray(maxNumberOfDetections) })
        put(COUNT_INDEX, FloatArray(1))
    }

    fun getRecognitions(labels: List<String>, inputSize: Size, scoreThreshold: Float): List<Recognition> =
        arrayListOf<Recognition>().apply {
            repeat(min(maxNumberOfDetections, getDetectionCount())) { detectionNumber ->
                getScore(detectionNumber)
                    .takeIf { it >= scoreThreshold }
                    ?.let {
                        add(
                            Recognition(
                                id = detectionNumber.toString(),
                                title = labels[getClassIndex(detectionNumber)],
                                confidence = it,
                                location = getLocation(detectionNumber, inputSize)
                            )
                        )
                    }
            }
        }

    private fun getLocation(detectionIndex: Int, inputSize: Size): RectF =
        (((valuesMap[LOCATIONS_INDEX]!! as Array<*>)[0] as Array<*>)[detectionIndex] as FloatArray).let { location ->
            RectF(
                location[1] * inputSize.width,
                location[0] * inputSize.height,
                location[3] * inputSize.width,
                location[2] * inputSize.height
            )
        }

    private fun getClassIndex(detectionIndex: Int): Int =
        (((valuesMap[CLASSES_INDEX]!! as Array<*>)[0] as FloatArray)[detectionIndex]).toInt()

    private fun getScore(detectionIndex: Int): Float =
        (((valuesMap[SCORES_INDEX]!! as Array<*>)[0] as FloatArray)[detectionIndex])

    private fun getDetectionCount(): Int =
        ((valuesMap[COUNT_INDEX]!! as FloatArray)[0]).toInt()
}
