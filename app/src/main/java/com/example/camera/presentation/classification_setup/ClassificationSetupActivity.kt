package com.example.camera.presentation.classification_setup

import android.content.Intent
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivityClassificationSetupBinding
import com.example.camera.presentation.base.BaseActivity

class ClassificationSetupActivity : BaseActivity<ActivityClassificationSetupBinding, ClassificationSetupViewModel>() {

    companion object {
        fun createIntent() = Intent(CameraApp.appContext, ClassificationSetupActivity::class.java)
    }

    override fun provideLayoutId() = R.layout.activity_classification_setup

    override fun provideViewModelClass() = ClassificationSetupViewModel::class.java
}
