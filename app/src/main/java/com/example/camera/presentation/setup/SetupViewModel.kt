package com.example.camera.presentation.setup

import android.util.Size
import com.example.camera.presentation.base.BaseViewModel
import com.example.camera.processing.Settings

class SetupViewModel : BaseViewModel<SetupViewModel.SetupAction>() {

    enum class SetupAction {
        PROCEED
    }

    private var settings = Settings()
    private val _resolutions = listOf(
        Size(480, 640),
        Size(300, 400)
    )

    val resolutions: List<String> get() = _resolutions.map { "${it.width}x${it.height}" }

    fun onResolutionSelected(index: Int) {
        if (index !in _resolutions.indices) return

        _resolutions[index].let { size ->
            settings.imageWidth = size.width
            settings.imageHeight = size.height
        }
    }

    fun onProceedClick() = setAction(SetupAction.PROCEED)

}
