package com.example.camera.presentation.mode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.camera.R
import com.example.camera.presentation.base.BaseViewModel

class ModeViewModel : BaseViewModel<ModeViewModel.ModeAction>() {

    enum class ModeAction {
        CAR_BRAND_CLASSIFICATION,
        DETECTION
    }

    private val _selectedImageLive = MutableLiveData(R.drawable.drawing_with_classifications2)

    val selectedImageLive: LiveData<Int> get() = _selectedImageLive
    val classificationSelected: LiveData<Boolean> =
        Transformations.map(_selectedImageLive) { it == R.drawable.drawing_with_classifications2 }
    val detectionSelected: LiveData<Boolean> =
        Transformations.map(_selectedImageLive) { it == R.drawable.drawing_with_detections3 }

    fun onCarClassificationSelected() =
        _selectedImageLive.postValue(R.drawable.drawing_with_classifications2)

    fun onObjectDetectionSelected() =
        _selectedImageLive.postValue(R.drawable.drawing_with_detections3)

    fun onProceedButtonClick() =
        setAction(
            if (classificationSelected.value == true) ModeAction.CAR_BRAND_CLASSIFICATION
            else ModeAction.DETECTION
        )
}
