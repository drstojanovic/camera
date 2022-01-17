package com.example.camera.presentation.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivityMainBinding
import com.example.camera.presentation.base.BaseActivity
import com.example.camera.presentation.main.MainViewModel.MainAction
import com.example.camera.presentation.main.info.SettingsInfo
import com.example.camera.presentation.main.info.SettingsInfoDialog
import com.example.camera.processing.Settings
import com.example.camera.utils.CameraUtils
import com.example.camera.utils.OnSurfaceTextureAvailableListener
import com.example.camera.utils.TAG
import com.example.camera.utils.observe

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(), CameraUtils.CameraEventListener {

    companion object {
        private const val EXTRA_SETTINGS = "settings"

        fun createIntent(settings: Settings) =
            Intent(CameraApp.appContext!!, MainActivity::class.java).putExtra(EXTRA_SETTINGS, settings)
    }

    private val detectionAdapter by lazy { DetectionAdapter(resources) }
    private val cameraUtils by lazy { CameraUtils(this) }
    override val cameraContext: Context get() = this

    override fun provideLayoutId() = R.layout.activity_main

    override fun provideViewModelClass() = MainViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObservers()
        setupViews()
        intent.getParcelableExtra<Settings>(EXTRA_SETTINGS)?.let { settings ->
            viewModel.initImageProcessor(settings)
        }
    }

    private fun setObservers() =
        observe(viewModel.action) {
            when (it) {
                MainAction.SaveImage -> saveImage()
                MainAction.ProcessingFinished -> cameraUtils.onImageProcessed()
                is MainAction.ShowInfoDialog -> showInfoDialog(it.settingsInfo)
            }
        }

    private fun setupViews() {
        binding.textureView.surfaceTextureListener =
            OnSurfaceTextureAvailableListener { cameraUtils.setup(windowManager.defaultDisplay.rotation) }
        if (binding.recyclerDetections.adapter == null) {
            binding.recyclerDetections.adapter = detectionAdapter
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            cameraUtils.stopPreview()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error while closing camera: " + exc.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraUtils.dispose()
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
