package com.example.camera.presentation.setup

import android.util.Log
import android.util.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.processing.Settings
import com.example.camera.repository.SettingsRepository
import com.example.camera.utils.TAG

class SetupViewModel : BaseViewModel<SetupViewModel.SetupAction>() {

    enum class SetupAction {
        PROCEED,
        NO_INTERNET
    }

    private val settingsRepository = SettingsRepository()
    private val _settingsLive: MutableLiveData<Settings> = MutableLiveData()
    private val _isLocalInferenceLive = MutableLiveData(false)
    private val _isNetworkAvailableLive = MutableLiveData<Boolean>()
    private val resolutions = listOf(
        Size(160, 160),
        Size(300, 300),
        Size(512, 512),
        Size(640, 640)
    )

    lateinit var settings: Settings
        private set
    val resolutionsLabels: List<String> get() = resolutions.map { "${it.width}x${it.height}" }
    val settingsLive: LiveData<Settings> = _settingsLive
    val isLocalInferenceLive: LiveData<Boolean> = _isLocalInferenceLive
    val showNetworkWarningLive: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(_isLocalInferenceLive) { postValue(getNetworkWarningVisibility()) }
        addSource(_isNetworkAvailableLive) { postValue(getNetworkWarningVisibility()) }
    }
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

    fun onImageQualityChange(imageQuality: Int) {
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

    fun onThreadCountChange(threadCount: Int) {
        settings.threadCount = threadCount
    }

    fun onIpAddressChange(text: String) {
        settings.serverIpAddress = text
    }

    fun onPortChange(text: String) {
        settings.serverPort = text
    }

    fun onProceedClick() {
        if (!settings.localInference && _isNetworkAvailableLive.value == false) {
            setAction(SetupAction.NO_INTERNET)
            return
        }

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

    fun onNetworkStatusChange(isAvailable: Boolean) =
        _isNetworkAvailableLive.postValue(isAvailable)

    private fun getNetworkWarningVisibility() =
        _isLocalInferenceLive.value == false && _isNetworkAvailableLive.value == false

    private fun getSelectedResolutionIndex(imageWidth: Int, imageHeight: Int): Int =
        resolutions.indexOfFirst { it.width == imageWidth && it.height == imageHeight }
}
