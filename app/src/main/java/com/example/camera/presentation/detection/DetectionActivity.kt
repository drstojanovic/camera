package com.example.camera.presentation.detection

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivityDetectionBinding
import com.example.camera.presentation.base.BaseActivity
import com.example.camera.presentation.detection.DetectionViewModel.DetectionAction
import com.example.camera.presentation.detection.info.SettingsInfo
import com.example.camera.presentation.detection.info.SettingsInfoDialog
import com.example.camera.processing.Settings
import com.example.camera.utils.*

class DetectionActivity : BaseActivity<ActivityDetectionBinding, DetectionViewModel>(),
    CameraUtils.CameraEventListener {

    companion object {
        private const val EXTRA_SETTINGS = "settings"

        fun createIntent(settings: Settings) =
            Intent(CameraApp.appContext!!, DetectionActivity::class.java).putExtra(EXTRA_SETTINGS, settings)
    }

    private val detectionAdapter by lazy { DetectionAdapter(resources) }
    private val cameraUtils by lazy { CameraUtils(this) }
    private val networkStatus: NetworkStatus by lazy { NetworkStatus(this) }
    override val cameraContext: Context get() = this

    override fun provideLayoutId() = R.layout.activity_detection

    override fun provideViewModelClass() = DetectionViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObservers()
        setupViews()
        intent.getParcelableExtra<Settings>(EXTRA_SETTINGS)?.let { settings ->
            viewModel.initImageProcessor(settings)
        }
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
        binding.textureView.surfaceTextureListener =
            OnSurfaceTextureAvailableListener { cameraUtils.setup(displayCompat.rotation) }
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
        SettingsInfoDialog(this, settingsInfo).show()

    private fun saveImage() {
        cameraUtils.saveImage()
        showToast(R.string.detection_image_saved)
    }
}
