package com.example.camera.presentation.setup

import android.util.Log
import android.util.Size
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.R
import com.example.camera.domain.GetSettings
import com.example.camera.domain.StoreSettings
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.processing.Settings
import com.example.camera.utils.TAG

class SetupViewModel : BaseViewModel<SetupViewModel.SetupAction>() {

    sealed class SetupAction {
        object Proceed : SetupAction()
        class Error(@StringRes val message: Int) : SetupAction()
    }

    private val getSettings = GetSettings()
    private val storeSettings = StoreSettings()
    private val _settingsLive: MutableLiveData<Settings> = MutableLiveData()
    private val _isLocalInferenceLive = MutableLiveData(false)
    private val _isNetworkAvailableLive = MutableLiveData<Boolean>()
    private val _classificationModeLive = MutableLiveData<Boolean>()
    private val resolutions = listOf(
        Size(160, 160),
        Size(300, 300),
        Size(512, 512),
        Size(640, 640)
    )

    var settings: Settings? = null
        private set
    val classificationModeLive: LiveData<Boolean> get() = _classificationModeLive
    val resolutionsLabels: List<String> get() = resolutions.map { "${it.width}x${it.height}" }
    val settingsLive: LiveData<Settings> = _settingsLive
    val isLocalInferenceLive: LiveData<Boolean> = _isLocalInferenceLive
    val showNetworkWarningLive: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(_isLocalInferenceLive) { postValue(getNetworkWarningVisibility()) }
        addSource(_isNetworkAvailableLive) { postValue(getNetworkWarningVisibility()) }
    }
    val resolutionIndexLive: LiveData<Int>
        get() = Transformations.map(_settingsLive) { getSelectedResolutionIndex(it.imageWidth, it.imageHeight) }

    fun init(classificationMode: Boolean) {
        getStoredSettings(classificationMode)
        _classificationModeLive.postValue(classificationMode)
    }

    private fun getStoredSettings(isClassification: Boolean) =
        getSettings.execute(isClassification)
            .subscribe(
                onSuccess = {
                    this.settings = it.copy()
                    _settingsLive.postValue(it)
                    _isLocalInferenceLive.postValue(it.localInference)
                },
                onError = {
                    Log.e(TAG, it.message ?: it.toString())
                })

    fun onNetworkStatusChange(isAvailable: Boolean) =
        _isNetworkAvailableLive.postValue(isAvailable)

    fun onResolutionSelected(index: Int) {
        if (index !in resolutions.indices) return

        resolutions[index].let { size ->
            settings?.imageWidth = size.width
            settings?.imageHeight = size.height
        }
    }

    fun onMaxDetectionLimitChange(detectionLimit: Int) {
        settings?.maxDetections = detectionLimit
    }

    fun onDetectionThresholdChange(confidenceThreshold: Int) {
        settings?.detectionThreshold = confidenceThreshold
    }

    fun onClassificationThresholdChange(confidenceThreshold: Int) {
        settings?.classificationThreshold = confidenceThreshold
    }

    fun onImageQualityChange(imageQuality: Int) {
        settings?.imageQuality = imageQuality
    }

    fun onLocalInferenceSelected() {
        _isLocalInferenceLive.postValue(true)
        settings?.localInference = true
    }

    fun onRemoteInferenceSelected() {
        _isLocalInferenceLive.postValue(false)
        settings?.localInference = false
    }

    fun onThreadCountChange(threadCount: Int) {
        settings?.threadCount = threadCount
    }

    fun onIpAddressChange(text: String) {
        settings?.serverIpAddress = text
    }

    fun onPortChange(text: String) {
        settings?.serverPort = text
    }

    fun onProceedClick() {
        if (!checkSetupValidity()) return

        if (settings == settingsLive.value) {
            setAction(SetupAction.Proceed)
        } else {
            settings?.let {
                storeSettings.execute(it, _classificationModeLive.value ?: false)
                    .subscribe(
                        onComplete = { setAction(SetupAction.Proceed) },
                        onError = { throwable ->
                            Log.e(TAG, throwable.message ?: throwable.toString())
                        })
            }
        }
    }

    private fun checkSetupValidity(): Boolean {
        if (settings?.localInference == false) {
            when {
                _isNetworkAvailableLive.value == false -> {
                    setAction(SetupAction.Error(R.string.setup_error_no_internet_connection))
                    return false
                }
                settings?.serverIpAddress.isNullOrEmpty() -> {
                    setAction(SetupAction.Error(R.string.setup_error_no_server_address))
                    return false
                }
                settings?.serverPort.isNullOrEmpty() -> {
                    setAction(SetupAction.Error(R.string.setup_error_no_server_port))
                    return false
                }
            }
        }
        return true
    }

    private fun getNetworkWarningVisibility() =
        _isLocalInferenceLive.value == false && _isNetworkAvailableLive.value == false

    private fun getSelectedResolutionIndex(imageWidth: Int, imageHeight: Int): Int =
        resolutions.indexOfFirst { it.width == imageWidth && it.height == imageHeight }
}
