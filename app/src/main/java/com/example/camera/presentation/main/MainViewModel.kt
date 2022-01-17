package com.example.camera.presentation.main

import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.CameraApp
import com.example.camera.detection.ProcessingResult
import com.example.camera.detection.Recognition
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.presentation.base.SingleLiveEvent
import com.example.camera.presentation.main.info.SettingsInfo
import com.example.camera.presentation.main.info.toSettingsInfo
import com.example.camera.processing.*
import com.example.camera.utils.TAG

class MainViewModel : BaseViewModel<MainViewModel.MainAction>() {

    sealed class MainAction {
        object SaveImage : MainAction()
        object ProcessingFinished : MainAction()
        class ShowInfoDialog(val settingsInfo: SettingsInfo) : MainAction()
    }

    private lateinit var imageProcessor: ImageProcessor
    private val _recognitionsLive = MutableLiveData<List<Recognition>>(listOf())
    private val _selectedImageSizeLive = SingleLiveEvent<Size>()
    private val _processingResultLive = MutableLiveData<ProcessingResult>()
    private val _showErrorLive = SingleLiveEvent<Boolean>()

    val selectedImageSizeLive: LiveData<Size> get() = _selectedImageSizeLive
    val processingResultLive: LiveData<ProcessingResult> get() = _processingResultLive
    val showErrorLive: LiveData<Boolean> get() = _showErrorLive
    val recognitionsLive: LiveData<List<Recognition>> = _recognitionsLive
    val recognitionLabelsLive: LiveData<List<String>> =
        Transformations.map(_recognitionsLive) { recognitions -> recognitions.map { it.toShortString() } }
    val hasDataLive: LiveData<Boolean> =
        Transformations.map(_recognitionsLive) { it.isNotEmpty() }

    fun initImageProcessor(settings: Settings) {
        imageProcessor =
            if (settings.localInference) LocalImageProcessor(CameraApp.appContext!!, settings)
            else RemoteImageProcessor(settings)
        _selectedImageSizeLive.postValue(settings.imageSize)
    }

    fun onImageAvailable(bitmap: Bitmap, orientation: Int) {
        imageProcessor.processImage(bitmap, orientation)
            .subscribe(
                onSuccess = { result: ProcessingResult ->
                    _recognitionsLive.postValue(result.recognitions)
                    _processingResultLive.postValue(result)
                    setAction(MainAction.ProcessingFinished)
                    _showErrorLive.postValue(false)
                },
                onError = { throwable ->
                    Log.e(TAG, throwable.message ?: throwable.toString())
                    setAction(MainAction.ProcessingFinished)
                    _showErrorLive.postValue(throwable is SocketDisconnectedException)
                }
            )
    }

    fun onSaveImageClick() =
        setAction(MainAction.SaveImage)

    fun onShowInfoSelected() =
        setAction(MainAction.ShowInfoDialog(imageProcessor.settings.toSettingsInfo()))

    override fun onCleared() {
        super.onCleared()
        imageProcessor.dispose()
    }
}
