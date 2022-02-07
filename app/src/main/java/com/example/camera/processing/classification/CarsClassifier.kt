package com.example.camera.processing.classification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.example.camera.processing.ImagePreprocessor
import com.example.camera.processing.Settings
import com.example.camera.processing.SocketManager
import com.example.camera.processing.detection.LocalObjectDetector
import com.example.camera.processing.detection.ObjectDetector
import com.example.camera.utils.EVENT_CLASSIFY_CARS
import com.example.camera.utils.ImageUtils.getByteArray
import com.example.camera.utils.filterItems
import com.example.camera.utils.mapItems
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Single

class CarsClassifier(
    context: Context,
    settings: Settings,
    private val objectDetector: ObjectDetector = LocalObjectDetector(context, settings)
) : MultipleObjectClassifier(settings) {

    private val socketManager: SocketManager = SocketManager(settings.serverAddressFull, settings.toQuery())
    private val classificationAdapter: JsonAdapter<List<ClassificationResultRaw>> = Moshi.Builder().build()
        .adapter(Types.newParameterizedType(List::class.java, ClassificationResultRaw::class.java))

    override fun dispose() {
        socketManager.dispose()
        objectDetector.dispose()
    }

    override fun process(image: Bitmap): Single<List<ClassificationResult>> =
        objectDetector.detectObjects(image.getByteArray(settings.imageQuality))
            .filterItems { it.title.trim() == "car" }
            .mapItems { it.location }
            .map { getImageCrops(image, it) }
            .flatMap { uploadCropsForClassification(it) }

    private fun getImageCrops(image: Bitmap, boundingBoxes: List<RectF>): List<ImageCrop> =
        boundingBoxes.map { box ->
            ImageCrop(
                location = box,
                encodedBytes =
                Bitmap.createBitmap(image, box.left.toInt(), box.top.toInt(), box.width().toInt(), box.height().toInt())
                    .getEncodedBytes()
            )
        }

    private fun Bitmap.getEncodedBytes(): ByteArray =
        ImagePreprocessor.encodeBytes(this.getByteArray(settings.imageQuality))

    private fun uploadCropsForClassification(crops: List<ImageCrop>): Single<List<ClassificationResult>> =
        socketManager.emitEvent(EVENT_CLASSIFY_CARS, *(crops.map { it.encodedBytes }.toTypedArray()))
            .map { result ->
                Log.d("sentic", "result: ${result[0]}")
                if (result.isNotEmpty() && result[0] != "") {
                    classificationAdapter.fromJson(result[0].toString())?.let { classifications ->
                        classifications.mapIndexed { index, classificationRaw ->
                            classificationRaw.toClassificationResult(index, crops[index].location)
                        }
                    }
                } else listOf()
            }
}
