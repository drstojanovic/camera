package com.example.camera.presentation.detection

import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.processing.detection.ProcessingResult
import com.example.camera.processing.detection.Recognition
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.presentation.base.SingleLiveEvent
import com.example.camera.presentation.detection.info.SettingsInfo
import com.example.camera.presentation.detection.info.toSettingsInfo
import com.example.camera.processing.*
import com.example.camera.utils.TAG
import java.util.concurrent.TimeoutException

class DetectionViewModel : BaseViewModel<DetectionViewModel.DetectionAction>() {

    sealed class DetectionAction {
        object SaveImage : DetectionAction()
        class ProcessingFinished(val error: Int? = null) : DetectionAction()
        class ShowInfoDialog(val settingsInfo: SettingsInfo) : DetectionAction()
    }

    private lateinit var imageProcessor: ImageProcessor
    private var hasNetwork = false
    private val _recognitionsLive = MutableLiveData<List<Recognition>>(listOf())
    private val _selectedImageSizeLive = SingleLiveEvent<Size>()
    private val _processingResultLive = MutableLiveData<ProcessingResult>()
    private val _errorLive = SingleLiveEvent<Int?>()

    val selectedImageSizeLive: LiveData<Size> get() = _selectedImageSizeLive
    val processingResultLive: LiveData<ProcessingResult> get() = _processingResultLive
    val errorLive: LiveData<Int?> get() = _errorLive
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
                    setAction(DetectionAction.ProcessingFinished())
                    _errorLive.postValue(null)
                },
                onError = { throwable ->
                    Log.e(TAG, throwable.message ?: throwable.toString())
                    handleProcessingError(throwable)
                }
            )
    }

    fun onSaveImageClick() =
        setAction(DetectionAction.SaveImage)

    fun onShowInfoSelected() =
        setAction(DetectionAction.ShowInfoDialog(imageProcessor.settings.toSettingsInfo()))

    fun onNetworkStatusChange(hasNetwork: Boolean) {
        this.hasNetwork = hasNetwork
        if (!hasNetwork) {
            _recognitionsLive.postValue(listOf())
            _errorLive.postValue(R.string.setup_error_no_internet_connection)
            setAction(DetectionAction.ProcessingFinished())
        } else {
            _errorLive.postValue(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageProcessor.dispose()
    }

    private fun handleProcessingError(throwable: Throwable) {
        if (hasNetwork && throwable is TimeoutException) {
            setAction(DetectionAction.ProcessingFinished(R.string.detection_error_bad_connection))
        } else if (hasNetwork && throwable is SocketDisconnectedException) {
            _errorLive.postValue(R.string.detection_error_unable_to_connect)
            setAction(DetectionAction.ProcessingFinished())
        } else {
            setAction(DetectionAction.ProcessingFinished())
        }
    }
}
