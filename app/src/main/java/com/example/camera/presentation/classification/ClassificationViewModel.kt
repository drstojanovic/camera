package com.example.camera.presentation.classification

import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.CameraApp
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.presentation.base.SingleLiveEvent
import com.example.camera.processing.Settings
import com.example.camera.processing.classification.CarsClassifier
import com.example.camera.processing.classification.ClassificationResult
import com.example.camera.processing.classification.MultipleObjectClassifier
import com.example.camera.processing.detection.Recognition
import com.example.camera.processing.detection.toRecognition
import com.example.camera.utils.TAG

class ClassificationViewModel : BaseViewModel<ClassificationViewModel.ClassificationAction>() {

    sealed class ClassificationAction {
        class ProcessingFinished(val error: Int? = null) : ClassificationAction()
    }

    private lateinit var multipleObjectClassifier: MultipleObjectClassifier
    private val _selectedImageSizeLive = SingleLiveEvent<Size>()
    private val _classificationResultLive = MutableLiveData<List<ClassificationResult>>()

    val selectedImageSizeLive: LiveData<Size> get() = _selectedImageSizeLive
    val classificationResultLive: LiveData<List<ClassificationResult>> = _classificationResultLive
    val classifiedObjectsLive: LiveData<List<Recognition>> =
        Transformations.map(_classificationResultLive) { resultList -> resultList.map { it.toRecognition() } }

    fun init(settings: Settings) {
        multipleObjectClassifier = CarsClassifier(CameraApp.appContext!!, settings)
        _selectedImageSizeLive.postValue(settings.imageSize)
    }

    fun onImageAvailable(bitmap: Bitmap, orientation: Int) {
        multipleObjectClassifier.processImage(bitmap, orientation)
            .subscribe(
                onSuccess = {
                    _classificationResultLive.postValue(it)
                    setAction(ClassificationAction.ProcessingFinished())
                },
                onError = { throwable ->
                    Log.e(TAG, throwable.message ?: throwable.toString())
                    setAction(ClassificationAction.ProcessingFinished())
                }
            )
    }

    fun onNetworkStatusChange(hasNetwork: Boolean) {

    }

    override fun onCleared() {
        multipleObjectClassifier.dispose()
    }
}
