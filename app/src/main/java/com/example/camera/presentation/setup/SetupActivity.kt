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
import com.example.camera.presentation.main.MainActivity
import com.example.camera.utils.NetworkStatus
import com.example.camera.utils.observe

class SetupActivity : BaseActivity<ActivitySetupBinding, SetupViewModel>() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        fun createIntent() = Intent(CameraApp.appContext, SetupActivity::class.java)
    }

    private val networkStatus: NetworkStatus by lazy { NetworkStatus(this) }

    override fun provideLayoutId() = R.layout.activity_setup

    override fun provideViewModelClass() = SetupViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkStatus.dispose()
    }

    private fun setObservers() {
        observe(networkStatus.asLiveData()) { viewModel.onNetworkStatusChange(it) }
        observe(viewModel.action) { action ->
            when (action) {
                SetupViewModel.SetupAction.PROCEED -> checkPermissionsAndProceed()
                SetupViewModel.SetupAction.NO_INTERNET -> showToast(R.string.setup_error_no_internet_connection)
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

    private fun proceedToCameraScreen() = startActivity(MainActivity.createIntent(viewModel.settings))
}
