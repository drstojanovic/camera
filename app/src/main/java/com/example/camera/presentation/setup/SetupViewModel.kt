package com.example.camera.presentation.setup

import android.util.Log
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

    private lateinit var settings: Settings
    private val settingsRepository = SettingsRepository()
    private val _settingsLive: MutableLiveData<Settings> = MutableLiveData()
    private val _maxDetectionsLive: MutableLiveData<Int> = MutableLiveData(0)
    private val _confidenceThresholdLive: MutableLiveData<Int> = MutableLiveData(0)

    val setupData = SetupData()
    val settingsLive: LiveData<Settings> = _settingsLive
    val maxDetectionsLive: LiveData<String>
        get() = Transformations.map(_maxDetectionsLive) { it.toString() }
    val confidenceThresholdLive: LiveData<String>
        get() = Transformations.map(_confidenceThresholdLive) { it.toString() }
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
                    _maxDetectionsLive.postValue(it.maxDetections)
                    _confidenceThresholdLive.postValue(it.confidenceThreshold)
                },
                onError = {
                    Log.e(TAG, it.message ?: it.toString())
                })

    fun onResolutionSelected(index: Int) {
        if (index !in setupData.resolutions.indices) return

        setupData.resolutions[index].let { size ->
            settings.imageWidth = size.width
            settings.imageHeight = size.height
        }
    }

    fun onMaxDetectionLimitChange(detectionLimit: Int) {
        settings.maxDetections = detectionLimit
        _maxDetectionsLive.postValue(detectionLimit)
    }

    fun onConfidenceThresholdChange(confidenceThreshold: Int) {
        settings.confidenceThreshold = confidenceThreshold
        _confidenceThresholdLive.postValue(confidenceThreshold)
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
        setupData.resolutions.indexOfFirst { it.width == imageWidth && it.height == imageHeight }
}
