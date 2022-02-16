package com.example.camera.presentation.classification

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.navigation.fragment.navArgs
import com.example.camera.R
import com.example.camera.databinding.FragmentClassificationBinding
import com.example.camera.presentation.base.BaseFragment
import com.example.camera.presentation.classification.ClassificationViewModel.ClassificationAction.*
import com.example.camera.presentation.detection.info.SettingsInfoDialog
import com.example.camera.utils.*

class ClassificationFragment : BaseFragment<FragmentClassificationBinding, ClassificationViewModel>(),
    CameraUtils.CameraEventListener {

    private val args: ClassificationFragmentArgs by navArgs()
    private val adapter by lazy { ClassificationAdapter() }
    private val cameraUtils by lazy { CameraUtils(this) }
    private val networkStatus: NetworkStatus by lazy { NetworkStatus(context) }
    override val cameraContext get() = requireContext()

    override fun provideLayoutId() = R.layout.fragment_classification

    override fun provideViewModelClass() = ClassificationViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setObservers()
        viewModel.init(args.settings)
    }

    private fun setupViews() {
        displayCompat?.rotation?.let {
            binding.textureView.surfaceTextureListener = OnSurfaceTextureAvailableListener { cameraUtils.setup(it) }
        }
        if (binding.recyclerDetections.adapter == null) {
            binding.recyclerDetections.adapter = adapter
        }
    }

    private fun setObservers() {
        observe(networkStatus.asLiveData()) { viewModel.onNetworkStatusChange(it) }
        observe(viewModel.action) {
            when (it) {
                PauseProcessing -> cameraUtils.freezeCameraPreview()
                ResumeProcessing -> cameraUtils.startCameraPreview()
                is ProcessingFinished -> {
                    cameraUtils.onImageProcessed()
                    it.error?.let { errorMsg -> showToast(errorMsg) }
                }
                is ShowInfoDialog -> context?.let { ctx ->
                    SettingsInfoDialog(ctx, it.settingsInfo).show()
                }
            }
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
}
