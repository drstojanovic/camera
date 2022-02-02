package com.example.camera.presentation.mode

import android.content.Intent
import android.os.Bundle
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivityModeChooserBinding
import com.example.camera.presentation.base.BaseActivity
import com.example.camera.presentation.classification_setup.ClassificationSetupActivity
import com.example.camera.presentation.detection_setup.DetectionSetupActivity
import com.example.camera.utils.observe
import com.example.camera.presentation.mode.ModeViewModel.ModeAction

class ModeActivity : BaseActivity<ActivityModeChooserBinding, ModeViewModel>() {

    companion object {
        fun createIntent() =
            Intent(CameraApp.appContext!!, ModeActivity::class.java)
    }

    override fun provideLayoutId() = R.layout.activity_mode_chooser

    override fun provideViewModelClass() = ModeViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observe(viewModel.action) {
            when (it) {
                ModeAction.CAR_BRAND_CLASSIFICATION ->
                    startActivity(ClassificationSetupActivity.createIntent())
                ModeAction.DETECTION ->
                    startActivity(DetectionSetupActivity.createIntent())
            }
        }
    }
}
