package com.example.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.*
import android.os.*
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.camera.classification.Recognition
import com.example.camera.databinding.ActivityMainBinding
import com.example.camera.utils.CameraUtils
import com.example.camera.utils.OnSurfaceTextureAvailableListener
import com.example.camera.utils.TAG
import io.reactivex.disposables.CompositeDisposable

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
private const val RESULT_FORMAT = "%s %.2f"

class MainActivity : AppCompatActivity(), CameraUtils.CameraEventListener {

    private lateinit var binding: ActivityMainBinding
    private val imageProcessor by lazy { ImageProcessor(applicationContext) }
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textureView.surfaceTextureListener =
            OnSurfaceTextureAvailableListener { checkPermissionsAndInit() }
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
    }

    private fun checkPermissionsAndInit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasRequiredPermissions()) {
            cameraUtils.setup(windowManager.defaultDisplay.rotation)
        } else {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun hasRequiredPermissions() = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraUtils.setup(windowManager.defaultDisplay.rotation)
            } else {
                Toast.makeText(this, "Permission request denied!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun saveImage(view: View) {
        cameraUtils.saveImage()
        Toast.makeText(this, "Image saved.", LENGTH_SHORT).show()
    }

    override fun onError(text: String) =
        Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show()

    override fun provideTextureViewSurface() =
        Surface(binding.textureView.surfaceTexture)

    override fun onPreviewSizeSelected(size: Size) {
        with(binding.textureView) {
            setAspectRatio(size.height, size.width)
            surfaceTexture.setDefaultBufferSize(size.width, size.height)
            Log.d(TAG, "Texture View preview size after applying values: $width x $height")
            binding.viewTracker.setFrameSize(size)
        }
    }

    override fun onImageAvailable(bitmap: Bitmap, orientation: Int) {
        imageProcessor.processImage(bitmap, orientation)
            .subscribe(
                { result ->
                    if (result.isNotEmpty()) {
                        binding.viewTracker.setData(result)
                        displayResults(result)
                    }
                    cameraUtils.onImageProcessed()
                },
                { throwable ->
                    Log.e(TAG, throwable.stackTraceToString())
                    cameraUtils.onImageProcessed()
                }
            )
            .also { compositeDisposable.add(it) }
    }

    private fun displayResults(result: List<Recognition>) {
        Log.d("sentic", result.toString())
        if (result.isNotEmpty())
            binding.txtResult1.text = RESULT_FORMAT.format(result[0].title, result[0].confidence * 100)
        if (result.size > 1)
            binding.txtResult2.text = RESULT_FORMAT.format(result[1].title, result[1].confidence * 100)
        if (result.size > 2)
            binding.txtResult3.text = RESULT_FORMAT.format(result[2].title, result[2].confidence * 100)
    }
}
