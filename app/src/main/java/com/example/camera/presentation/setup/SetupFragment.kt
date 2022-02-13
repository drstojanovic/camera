package com.example.camera.presentation.setup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.camera.R
import com.example.camera.databinding.FragmentSetupBinding
import com.example.camera.presentation.base.BaseFragment
import com.example.camera.presentation.setup.SetupViewModel.SetupAction
import com.example.camera.utils.NetworkStatus
import com.example.camera.utils.observe

class SetupFragment : BaseFragment<FragmentSetupBinding, SetupViewModel>() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
    }

    private val args: SetupFragmentArgs by navArgs()
    private val networkStatus: NetworkStatus by lazy { NetworkStatus(context) }

    override fun provideLayoutId() = R.layout.fragment_setup

    override fun provideViewModelClass() = SetupViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        viewModel.init(args.isClassificationMode)
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
        context
            ?.let { ctx -> ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED }
            ?: false
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
        findNavController().navigate(
            if (args.isClassificationMode) {
                SetupFragmentDirections.actionSetupFragmentToClassificationFragment(settings)
            } else {
                SetupFragmentDirections.actionSetupFragmentToDetectionFragment(settings)
            }
        )
    }
}
