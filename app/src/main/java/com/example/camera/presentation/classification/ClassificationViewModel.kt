package com.example.camera.presentation.classification

import android.graphics.Bitmap
import com.example.camera.CameraApp
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.processing.Settings
import com.example.camera.processing.classification.CarsClassifier
import com.example.camera.processing.classification.MultipleObjectClassifier

class ClassificationViewModel : BaseViewModel<ClassificationViewModel.ClassificationAction>() {

    sealed class ClassificationAction {
        class ProcessingFinished(val error: Int? = null) : ClassificationAction()
    }

    private lateinit var multipleObjectClassifier: MultipleObjectClassifier

    fun init(settings: Settings) {
        multipleObjectClassifier = CarsClassifier(CameraApp.appContext!!, settings)
    }

    fun onImageAvailable(bitmap: Bitmap, orientation: Int) {
        multipleObjectClassifier.processImage(bitmap, orientation)
            .subscribe(
                onSuccess = {
                    setAction(ClassificationAction.ProcessingFinished())
                },
                onError = {
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
