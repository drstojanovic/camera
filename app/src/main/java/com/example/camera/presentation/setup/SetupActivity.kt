package com.example.camera.presentation.setup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivitySetupBinding
import com.example.camera.presentation.base.BaseActivity
import com.example.camera.presentation.classification.ClassificationActivity
import com.example.camera.presentation.detection.DetectionActivity
import com.example.camera.utils.NetworkStatus
import com.example.camera.utils.observe
import com.example.camera.presentation.setup.SetupViewModel.SetupAction

class SetupActivity : BaseActivity<ActivitySetupBinding, SetupViewModel>() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
        private const val EXTRA_CLASSIFICATION_MODE = "classification_mode"
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        fun createIntent(classificationMode: Boolean = false) =
            Intent(CameraApp.appContext, SetupActivity::class.java)
                .putExtra(EXTRA_CLASSIFICATION_MODE, classificationMode)
    }

    private val networkStatus: NetworkStatus by lazy { NetworkStatus(this) }

    override fun provideLayoutId() = R.layout.activity_setup

    override fun provideViewModelClass() = SetupViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObservers()
        viewModel.init(intent.getBooleanExtra(EXTRA_CLASSIFICATION_MODE, false))
    }

    override fun onDestroy() {
        super.onDestroy()
        networkStatus.dispose()
    }

    private fun setObservers() {
        observe(networkStatus.asLiveData()) { viewModel.onNetworkStatusChange(it) }
        observe(viewModel.action) { action ->
            when (action) {
                SetupAction.Proceed -> checkPermissionsAndProceed()
                is SetupAction.Error -> showToast(action.message)
            }
        }
    }

    private fun checkPermissionsAndProceed() =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasRequiredPermissions()) {
            proceedToCameraScreen()
        } else {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }

    private fun hasRequiredPermissions() = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToCameraScreen()
            } else {
                showToast(R.string.setup_error_permission_not_granted, Toast.LENGTH_LONG)
            }
        }
    }

    private fun proceedToCameraScreen() = viewModel.settings?.let { settings ->
        startActivity(
            if (intent.getBooleanExtra(EXTRA_CLASSIFICATION_MODE, false)) {
                ClassificationActivity.createIntent(settings)
            } else {
                DetectionActivity.createIntent(settings)
            }
        )
    }
}
