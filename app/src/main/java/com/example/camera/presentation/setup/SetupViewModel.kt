package com.example.camera.presentation.setup

import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.processing.Settings
import com.example.camera.repository.SettingsRepository
import com.example.camera.utils.TAG

class SetupViewModel : BaseViewModel<SetupViewModel.SetupAction>() {

    enum class SetupAction {
        PROCEED
    }

    private val settingsRepository = SettingsRepository()
    private val _settingsLive: MutableLiveData<Settings> = MutableLiveData()
    private val _isLocalInferenceLive = MutableLiveData(false)
    private val resolutions = listOf(Size(512, 512), Size(300, 300))

    lateinit var settings: Settings
        private set
    val resolutionsLabels: List<String> get() = resolutions.map { "${it.width}x${it.height}" }
    val settingsLive: LiveData<Settings> = _settingsLive
    val isLocalInferenceLive: LiveData<Boolean> = _isLocalInferenceLive
    val resolutionIndexLive: LiveData<Int>
        get() = Transformations.map(_settingsLive) { getSelectedResolutionIndex(it.imageWidth, it.imageHeight) }

    init {
        getStoredSettings()
    }

    private fun getStoredSettings() =
        settingsRepository.getSettings()
            .subscribe(
                onSuccess = {
                    this.settings = it.copy()
                    _settingsLive.postValue(it)
                    _isLocalInferenceLive.postValue(it.localInference)
                },
                onError = {
                    Log.e(TAG, it.message ?: it.toString())
                })

    fun onResolutionSelected(index: Int) {
        if (index !in resolutions.indices) return

        resolutions[index].let { size ->
            settings.imageWidth = size.width
            settings.imageHeight = size.height
        }
    }

    fun onMaxDetectionLimitChange(detectionLimit: Int) {
        settings.maxDetections = detectionLimit
    }

    fun onConfidenceThresholdChange(confidenceThreshold: Int) {
        settings.confidenceThreshold = confidenceThreshold
    }

    fun onImageQualityChange(imageQuality:Int) {
        settings.imageQuality = imageQuality
    }

    fun onLocalInferenceSelected() {
        _isLocalInferenceLive.postValue(true)
        settings.localInference = true
    }

    fun onRemoteInferenceSelected() {
        _isLocalInferenceLive.postValue(false)
        settings.localInference = false
    }

    fun onIpAddressChange(text: String) {
        settings.serverIpAddress = text
    }

    fun onPortChange(text: String) {
        settings.serverPort = text
    }

    fun onProceedClick() {
        if (settings == settingsLive.value) {
            setAction(SetupAction.PROCEED)
            return
        }

        settingsRepository.storeSettings(settings)
            .subscribe(
                onComplete = { setAction(SetupAction.PROCEED) },
                onError = {
                    Log.e(TAG, it.message ?: it.toString())
                })
    }

    private fun getSelectedResolutionIndex(imageWidth: Int, imageHeight: Int): Int =
        resolutions.indexOfFirst { it.width == imageWidth && it.height == imageHeight }
}
