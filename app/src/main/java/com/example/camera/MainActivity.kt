package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Point
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

private const val TAG = "CameraTAG"
private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
private val SIZE_1080P = SmartSize(1920, 1080)

class MainActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener {

    private val cameraThread = HandlerThread("Camera Thread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var camera: CameraDevice
    private lateinit var previewSize: Size
    private lateinit var surfacePreview: AutoFitSurfaceView
    private val cameraManager: CameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForPermissionsAndInitViews()
    }

    // region Tear down methods
    override fun onStop() {
        super.onStop()
        try {
            camera.close()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error while closing camera")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
    }
    // endregion

    // region Permissions
    private fun checkForPermissionsAndInitViews() {
        if (hasRequiredPermissions()) {
            initViews()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
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

    private fun hasRequiredPermissions() = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    // endregion

    private fun initViews() {
        surfacePreview = findViewById(R.id.surface_preview)
        surfacePreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) = Unit
            override fun surfaceDestroyed(holder: SurfaceHolder?) = Unit
            override fun surfaceCreated(holder: SurfaceHolder?) {
                setupCamera()
            }
        })
    }

    private fun setupCamera() {
        val cameraId = getCameraId()
        if (cameraId == null) {
            Toast.makeText(this, "No cameras available!", LENGTH_SHORT).show()
            return
        }

        previewSize = getPreviewOutputSize(cameraId)
        Log.d(TAG, "Surface View preview size: ${surfacePreview.width} x ${surfacePreview.height}")
        Log.d(TAG, "Selected preview size: $previewSize")
        surfacePreview.holder.setFixedSize(previewSize.width, previewSize.height)
        surfacePreview.setAspectRatio(previewSize.width, previewSize.height)
        openCamera(cameraId)
    }

    private fun initPreviewSession() {
        // set buffer size
        val imageReader = ImageReader.newInstance(
                previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 2)
                .apply {
                    setOnImageAvailableListener(this@MainActivity, cameraHandler)
                }
        
        val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(surfacePreview.holder.surface)
            addTarget(imageReader.surface)
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)  // auto-focus
            set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)       // flash
        }

        camera.createCaptureSession(listOf(surfacePreview.holder.surface, imageReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        val exc = RuntimeException("Camera ${camera.id} session configuration failed")
                        Log.e(TAG, exc.message, exc)
                    }
                }, cameraHandler)
    }

    override fun onImageAvailable(reader: ImageReader?) {
        Log.d(TAG, "image acquired " + reader?.imageFormat)
    }

    // region Helper Methods
    @SuppressLint("MissingPermission")
    private fun openCamera(cameraId: String) {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                this@MainActivity.camera = camera
                initPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.w(TAG, "Camera $cameraId has been disconnected")
                finish()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e(TAG, exc.message, exc)

            }
        }, cameraHandler)
    }

    private fun getPreviewOutputSize(cameraId: String): Size {
        val screenSize = getDisplaySize()
        val hdScreen = screenSize.long >= SIZE_1080P.long || screenSize.short >= SIZE_1080P.short
        val maxAllowedSize = if (hdScreen) SIZE_1080P else screenSize

        return cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                .getOutputSizes(SurfaceHolder::class.java)
                .sortedWith(compareBy { it.height * it.width })
                .map { SmartSize(it.width, it.height) }
                .reversed()
                .first { it.long <= maxAllowedSize.long && it.short <= maxAllowedSize.short }.size
    }

    private fun getDisplaySize() = Point().let {
        surfacePreview.display.getRealSize(it)
        SmartSize(it.x, it.y)
    }

    private fun getCameraId(): String? = cameraManager.run {
        cameraIdList.firstOrNull { id ->
            val characteristics = getCameraCharacteristics(id)
            val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK &&
                    (capabilities?.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)
                            ?: false)

        }
    }

    // endregion

}
