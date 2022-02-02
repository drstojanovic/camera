package com.example.camera.presentation.mode

import com.example.camera.presentation.base.BaseViewModel

class ModeViewModel : BaseViewModel<ModeViewModel.ModeAction>() {

    enum class ModeAction {
        CAR_BRAND_CLASSIFICATION,
        DETECTION
    }

    fun onCarClassificationSelected() = setAction(ModeAction.CAR_BRAND_CLASSIFICATION)

    fun onObjectDetectionSelected() = setAction(ModeAction.DETECTION)
}
