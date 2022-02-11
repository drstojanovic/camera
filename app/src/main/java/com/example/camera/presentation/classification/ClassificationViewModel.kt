package com.example.camera.presentation.classification

import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.presentation.base.SingleLiveEvent
import com.example.camera.presentation.detection.info.SettingsInfo
import com.example.camera.presentation.detection.info.toSettingsInfo
import com.example.camera.processing.Settings
import com.example.camera.processing.classification.CarsClassifier
import com.example.camera.processing.classification.MultipleObjectClassifier
import com.example.camera.processing.detection.Recognition
import com.example.camera.processing.detection.toRecognition
import com.example.camera.utils.TAG

class ClassificationViewModel : BaseViewModel<ClassificationViewModel.ClassificationAction>() {

    sealed class ClassificationAction {
        object PauseProcessing : ClassificationAction()
        object ResumeProcessing : ClassificationAction()
        class ProcessingFinished(val error: Int? = null) : ClassificationAction()
        class ShowInfoDialog(val settingsInfo: SettingsInfo) : ClassificationAction()
    }

    private lateinit var multipleObjectClassifier: MultipleObjectClassifier
    private val _selectedImageSizeLive = SingleLiveEvent<Size>()
    private val _classificationResultLive = MutableLiveData<List<ClassificationResultView>>()
    private val _fabIconLive = MutableLiveData(R.drawable.ic_pause)
    private val colors = CameraApp.appContext?.resources?.getIntArray(R.array.bbox_colors)
    private var isPaused = false

    val selectedImageSizeLive: LiveData<Size> get() = _selectedImageSizeLive
    val fabIconLive: LiveData<Int> get() = _fabIconLive
    val classifiedObjectsLive: LiveData<List<ClassificationResultView>> = _classificationResultLive
    val detectedObjectsLive: LiveData<List<Recognition>> =
        Transformations.map(_classificationResultLive) { resultList -> resultList.map { it.toRecognition() } }

    fun init(settings: Settings) {
        multipleObjectClassifier = CarsClassifier(CameraApp.appContext!!, settings)
        _selectedImageSizeLive.postValue(settings.imageSize)
    }

    fun onImageAvailable(bitmap: Bitmap, orientation: Int) {
        if (isPaused) return

        multipleObjectClassifier.processImage(bitmap, orientation)
            .subscribe(
                onSuccess = {
                    colors?.let { colors ->
                        it.mapIndexed { index, result -> result.toClassificationResultView(colors[index]) }
                            .also { _classificationResultLive.postValue(it) }
                    }
                    setAction(ClassificationAction.ProcessingFinished())
                },
                onError = { throwable ->
                    Log.e(TAG, throwable.message ?: throwable.toString())
                    setAction(ClassificationAction.ProcessingFinished())
                }
            )
    }

    fun onShowInfoSelected() = setAction(
        ClassificationAction.ShowInfoDialog(multipleObjectClassifier.settings.toSettingsInfo(isClassification = true))
    )

    fun onPlayPauseClick() {
        isPaused = !isPaused
        setAction(if (isPaused) ClassificationAction.PauseProcessing else ClassificationAction.ResumeProcessing)
        _fabIconLive.postValue(if (isPaused) R.drawable.ic_play else R.drawable.ic_pause)
    }

    fun onNetworkStatusChange(hasNetwork: Boolean) {

    }

    override fun onCleared() {
        multipleObjectClassifier.dispose()
    }
}
