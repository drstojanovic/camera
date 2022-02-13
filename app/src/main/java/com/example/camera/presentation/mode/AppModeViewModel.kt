package com.example.camera.presentation.mode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.camera.presentation.base.BaseViewModel

class AppModeViewModel : BaseViewModel<AppModeViewModel.ModeAction>() {

    enum class ModeAction {
        CAR_BRAND_CLASSIFICATION,
        DETECTION
    }

    private val _selectedDetectionLive = MutableLiveData(true)

    val selectedDetectionLive: LiveData<Boolean> = _selectedDetectionLive

    fun onCarClassificationSelected() =
        _selectedDetectionLive.postValue(false)

    fun onObjectDetectionSelected() =
        _selectedDetectionLive.postValue(true)

    fun onProceedButtonClick() = setAction(
        if (_selectedDetectionLive.value == true) ModeAction.DETECTION else ModeAction.CAR_BRAND_CLASSIFICATION
    )
}
