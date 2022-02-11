package com.example.camera.presentation.classification

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivityClassificationBinding
import com.example.camera.presentation.base.BaseActivity
import com.example.camera.presentation.classification.ClassificationViewModel.ClassificationAction.*
import com.example.camera.presentation.detection.info.SettingsInfoDialog
import com.example.camera.processing.Settings
import com.example.camera.utils.*

class ClassificationActivity : BaseActivity<ActivityClassificationBinding, ClassificationViewModel>(),
    CameraUtils.CameraEventListener {

    companion object {
        private const val EXTRA_SETTINGS = "extra_settings"

        fun createIntent(settings: Settings) =
            Intent(CameraApp.appContext, ClassificationActivity::class.java)
                .putExtra(EXTRA_SETTINGS, settings)
    }

    private val adapter by lazy { ClassificationAdapter() }
    private val cameraUtils by lazy { CameraUtils(this) }
    private val networkStatus: NetworkStatus by lazy { NetworkStatus(this) }
    override val cameraContext get() = this

    override fun provideLayoutId() = R.layout.activity_classification

    override fun provideViewModelClass() = ClassificationViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        setObservers()
        intent.getParcelableExtra<Settings>(EXTRA_SETTINGS)?.let { settings ->
            viewModel.init(settings)
        }
    }

    private fun setupViews() {
        binding.textureView.surfaceTextureListener =
            OnSurfaceTextureAvailableListener { cameraUtils.setup(displayCompat.rotation) }
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
                is ShowInfoDialog ->
                    SettingsInfoDialog(this, it.settingsInfo).show()
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
