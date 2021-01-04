package com.example.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.*
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.camera.tflite.Classifier
import com.example.camera.utils.AutoFitTextureView
import com.example.camera.utils.CameraUtils
import com.example.camera.utils.TAG

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
private const val RESULT_FORMAT = "%s %.2f"

class MainActivity : AppCompatActivity() {

    private val cameraThread = HandlerThread("Camera Thread").apply { start() }
    private val imageReaderThread = HandlerThread("ImageReader Thread").apply { start() }

    private lateinit var textureView: AutoFitTextureView
    private lateinit var txtResult1: TextView
    private lateinit var txtResult2: TextView
    private lateinit var txtResult3: TextView

    private val cameraUtils by lazy {
        CameraUtils(
            applicationContext = this.applicationContext,
            cameraHandler = Handler(cameraThread.looper),
            imageReaderHandler = Handler(imageReaderThread.looper),
            eventListener = object : CameraUtils.EventListener {
                override fun onPreviewSizeSelected(size: Size) {
                    textureView.setAspectRatio(size.height, size.width)
                    textureView.surfaceTexture.setDefaultBufferSize(size.width, size.height)
                    Log.d(
                        TAG,
                        "Texture View preview size after applying values: ${textureView.width} x ${textureView.height}"
                    )
                }

                override fun onError(text: String) = Toast.makeText(this@MainActivity, text, LENGTH_SHORT).show()
                override fun provideTextureViewSurface() = Surface(textureView.surfaceTexture)
                override fun onProcessingResult(result: MutableList<Classifier.Recognition>) {
                    txtResult1.text = RESULT_FORMAT.format(result[0].title, result[0].confidence * 100)
                    txtResult2.text = RESULT_FORMAT.format(result[1].title, result[1].confidence * 100)
                    txtResult3.text = RESULT_FORMAT.format(result[2].title, result[2].confidence * 100)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForPermissionsAndInitViews()
        txtResult1 = findViewById(R.id.txt_result_1)
        txtResult2 = findViewById(R.id.txt_result_2)
        txtResult3 = findViewById(R.id.txt_result_3)
    }


    private fun initViews() {
        textureView = findViewById(R.id.texture_view)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) =
                cameraUtils.onTextureViewAvailable(windowManager.defaultDisplay.rotation)

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = false
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit
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
        cameraUtils.tearDown()
    }

    private fun checkForPermissionsAndInitViews() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasRequiredPermissions()) {
            initViews()
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
                initViews()
            } else {
                Toast.makeText(this, "Permission request denied!", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun saveImage(view: View) {
        cameraUtils.saveImage()
    }

}
