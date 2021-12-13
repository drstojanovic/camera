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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.camera.CameraApp
import com.example.camera.R
import com.example.camera.databinding.ActivityMainBinding
import com.example.camera.detection.ProcessingResult
import com.example.camera.detection.Recognition
import com.example.camera.processing.ImageProcessor
import com.example.camera.processing.LocalImageProcessor
import com.example.camera.processing.RemoteImageProcessor
import com.example.camera.processing.Settings
import com.example.camera.utils.CameraUtils
import com.example.camera.utils.OnSurfaceTextureAvailableListener
import com.example.camera.utils.TAG
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity(), CameraUtils.CameraEventListener {

    companion object {
        private const val EXTRA_SETTINGS = "settings"

        fun createIntent(settings: Settings) =
            Intent(CameraApp.appContext!!, MainActivity::class.java).putExtra(EXTRA_SETTINGS, settings)
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageProcessor: ImageProcessor
    private val detectionAdapter by lazy { DetectionAdapter(resources) }
    private val compositeDisposable = CompositeDisposable()
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initImageProcessor(intent.getParcelableExtra(EXTRA_SETTINGS)!!)
        setupViews()
    }

    private fun setupViews() {
        binding.textureView.surfaceTextureListener =
            OnSurfaceTextureAvailableListener { cameraUtils.setup(windowManager.defaultDisplay.rotation) }
        binding.fabCamera.setOnClickListener { saveImage() }
        if (binding.recyclerDetections.adapter == null) {
            binding.recyclerDetections.adapter = detectionAdapter
        }
    }

    private fun initImageProcessor(settings: Settings) {
        imageProcessor =
            if (settings.localInference) LocalImageProcessor(this, settings)
            else RemoteImageProcessor(settings)
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
        compositeDisposable.dispose()
        imageProcessor.dispose()
    }

    override fun onError(text: String) =
        Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show()

    override fun provideTextureViewSurface() =
        Surface(binding.textureView.surfaceTexture)

    override fun onPreviewSizeSelected(size: Size) {
        with(binding.textureView) {
            setAspectRatio(size.height, size.width)
            surfaceTexture?.setDefaultBufferSize(size.width, size.height)
            Log.d(TAG, "Texture View preview size after applying values: $width x $height")
            binding.viewTracker.setModelInputSize(imageProcessor.selectedInputSize)
        }
    }

    override fun onImageAvailable(bitmap: Bitmap, orientation: Int) {
        imageProcessor.processImage(bitmap, orientation)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result: ProcessingResult ->
                    binding.viewTracker.setData(result.recognitions)
                    displayResults(result.recognitions)
                    displayNumbers(result)
                    cameraUtils.onImageProcessed()
                },
                { throwable ->
                    Log.e(TAG, throwable.stackTraceToString())
                    cameraUtils.onImageProcessed()
                }
            )
            .also { compositeDisposable.add(it) }
    }

    private fun saveImage() {
        cameraUtils.saveImage()
        Toast.makeText(this, "Image saved.", LENGTH_SHORT).show()
    }

    private fun displayNumbers(result: ProcessingResult) {
        binding.txtInferenceLast.text = result.lastRecognitionTimeString
        binding.txtInferenceAverage.text = result.avgRecognitionTimeString
        binding.txtImageSizeLast.text = result.lastImageSizeBytesString
        binding.txtImageSizeAverage.text = result.avgImageSizeKbString
    }

    private fun displayResults(result: List<Recognition>) {
        Log.d(TAG, result.toString())
        detectionAdapter.setItems(result.map { it.toShortString() })
        binding.txtNoDetections.isVisible = result.isEmpty()
        binding.txtDetectionsLabel.isVisible = result.isNotEmpty()
    }
}
