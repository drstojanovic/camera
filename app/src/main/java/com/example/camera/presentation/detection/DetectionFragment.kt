package com.example.camera.presentation.detection

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.navigation.fragment.navArgs
import com.example.camera.R
import com.example.camera.databinding.FragmentDetectionBinding
import com.example.camera.presentation.base.BaseFragment
import com.example.camera.presentation.detection.DetectionViewModel.DetectionAction
import com.example.camera.presentation.detection.info.SettingsInfo
import com.example.camera.presentation.detection.info.SettingsInfoDialog
import com.example.camera.utils.*

class DetectionFragment : BaseFragment<FragmentDetectionBinding, DetectionViewModel>(),
    CameraUtils.CameraEventListener {

    private val args: DetectionFragmentArgs by navArgs()
    private val detectionAdapter by lazy { DetectionAdapter(resources) }
    private val cameraUtils by lazy { CameraUtils(this) }
    private val networkStatus: NetworkStatus by lazy { NetworkStatus(context) }
    override val cameraContext: Context get() = requireContext()

    override fun provideLayoutId() = R.layout.fragment_detection

    override fun provideViewModelClass() = DetectionViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setupViews()
        viewModel.initImageProcessor(args.settings)
    }

    private fun setObservers() {
        observe(networkStatus.asLiveData()) { viewModel.onNetworkStatusChange(it) }
        observe(viewModel.action) {
            when (it) {
                DetectionAction.SaveImage -> saveImage()
                is DetectionAction.ShowInfoDialog -> showInfoDialog(it.settingsInfo)
                is DetectionAction.ProcessingFinished -> {
                    cameraUtils.onImageProcessed()
                    it.error?.let { errorMsg -> showToast(errorMsg) }
                }
            }
        }
    }

    private fun setupViews() {
        displayCompat?.rotation?.let {
            binding.textureView.surfaceTextureListener = OnSurfaceTextureAvailableListener { cameraUtils.setup(it) }
        }
        if (binding.recyclerDetections.adapter == null) {
            binding.recyclerDetections.adapter = detectionAdapter
        }
    }

    override fun onError(text: String) = showToast(text)

    override fun provideTextureViewSurface() = Surface(binding.textureView.surfaceTexture)

    override fun onPreviewSizeSelected(size: Size) {
        with(binding.textureView) {
            setAspectRatio(size.height, size.width)
            surfaceTexture?.setDefaultBufferSize(size.width, size.height)
            Log.d(TAG, "Texture View preview size after applying values: $width x $height")
        }
    }

    override fun onImageAvailable(bitmap: Bitmap, orientation: Int) =
        viewModel.onImageAvailable(bitmap, orientation)

    private fun showInfoDialog(settingsInfo: SettingsInfo) =
        context?.let { SettingsInfoDialog(it, settingsInfo).show() }

    private fun saveImage() {
        cameraUtils.saveImage()
        showToast(R.string.detection_image_saved)
    }
}
