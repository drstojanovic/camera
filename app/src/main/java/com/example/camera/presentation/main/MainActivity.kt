package com.example.camera.presentation.main

import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.*
import android.os.*
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivityMainBinding
import com.example.camera.presentation.main.info.SettingsInfoDialog
import com.example.camera.presentation.main.info.toSettingsInfo
import com.example.camera.processing.*
import com.example.camera.utils.CameraUtils
import com.example.camera.utils.OnSurfaceTextureAvailableListener
import com.example.camera.utils.TAG
import com.example.camera.utils.observe

class MainActivity : AppCompatActivity(), CameraUtils.CameraEventListener {

    companion object {
        private const val EXTRA_SETTINGS = "settings"

        fun createIntent(settings: Settings) =
            Intent(CameraApp.appContext!!, MainActivity::class.java).putExtra(EXTRA_SETTINGS, settings)
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val detectionAdapter by lazy { DetectionAdapter(resources) }
    private val cameraThread = HandlerThread("Camera Thread").apply { start() }
    private val imageReaderThread = HandlerThread("ImageReader Thread").apply { start() }
    private val cameraUtils by lazy {
        CameraUtils(
            applicationContext = applicationContext,
            cameraHandler = Handler(cameraThread.looper),
            imageReaderHandler = Handler(imageReaderThread.looper),
            cameraHost = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setVariables()
        setObservers()
        setupViews()
        intent.getParcelableExtra<Settings>(EXTRA_SETTINGS)?.let { settings ->
            viewModel.initImageProcessor(settings)
        }
    }

    private fun setVariables() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.vm = viewModel
    }

    private fun setObservers() {
        observe(viewModel.action) {
            when (it) {
                MainViewModel.MainAction.SAVE_IMAGE -> saveImage()
                MainViewModel.MainAction.SHOW_INFO_DIALOG -> showInfoDialog()
                MainViewModel.MainAction.PROCESSING_FINISHED -> cameraUtils.onImageProcessed()
            }
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
        imageReaderThread.quitSafely()
        cameraThread.quitSafely()
    }

    override fun onError(text: String) =
        Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show()

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

    private fun showInfoDialog() =
        intent.getParcelableExtra<Settings>(EXTRA_SETTINGS)?.let { settings ->
            SettingsInfoDialog(this, settings.toSettingsInfo()).show()
        }

    private fun saveImage() {
        cameraUtils.saveImage()
        Toast.makeText(this, "Image saved.", LENGTH_SHORT).show()
    }
}
