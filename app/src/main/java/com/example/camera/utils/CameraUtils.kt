package com.example.camera.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.math.max
import kotlin.math.min

private val DESIRED_PREVIEW_SIZE = Size(640, 480)
private const val MINIMAL_VALID_PREVIEW_SIZE = 320  // empiric value

class CameraUtils(
    private val cameraHost: CameraEventListener
) : ImageReader.OnImageAvailableListener, DefaultLifecycleObserver {

    private lateinit var previewSize: Size
    private lateinit var camera: CameraDevice
    private lateinit var imageReader: ImageReader
    private var orientation: Int = 270
    private var isProcessing = false
    private var bitmap: Bitmap? = null
    private var selectedCameraId: String? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null
    private var imageReaderThread: HandlerThread? = null
    private var imageReaderHandler: Handler? = null
    private var session: CameraCaptureSession? = null
    private val cameraManager: CameraManager
            by lazy { cameraHost.cameraContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    init {
        initBackgroundThreads()
        (cameraHost as LifecycleOwner).lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        if (cameraThread == null || cameraThread?.isAlive == false) {
            initBackgroundThreads()
            selectedCameraId?.let { openCamera(it) }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        camera.close()
        destroyBackgroundThreads()
    }

    fun onImageProcessed() {
        isProcessing = false
    }

    fun saveImage() =
        ImageUtils.saveBitmap(cameraHost.cameraContext, bitmap, -orientation)

    fun freezeCameraPreview() =
        session?.stopRepeating()

    fun startCameraPreview() {
        isProcessing = false
        session?.setRepeatingRequest(
            getCaptureRequest(surface = cameraHost.provideTextureViewSurface()), null, cameraHandler
        )
    }

    fun setup(defaultDisplayOrientation: Int) =
        setupCamera(defaultDisplayOrientation)

    private fun setupCamera(defaultDisplayOrientation: Int) {
        val cameraId = getCameraId()
        if (cameraId == null) {
            cameraHost.onError("No appropriate camera available!")
            return
        }

        selectedCameraId = cameraId
        orientation = defaultDisplayOrientation - getSensorOrientation(cameraId)
        Log.d(TAG, "Orientation: $orientation")
        previewSize = getSmallestValidOutputSize(cameraId)
        Log.d(TAG, "Selected preview size: $previewSize")
        cameraHost.onPreviewSizeSelected(previewSize)
        openCamera(cameraId)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(cameraId: String) {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                Log.d(TAG, "Camera $cameraId is open")
                camera = cameraDevice
                initPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                Log.w(TAG, "Camera $cameraId has been disconnected")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }.let { reason ->
                    "Camera $cameraId error: ($error) $reason".let { message ->
                        Log.e(TAG, message, RuntimeException(message))
                        cameraHost.onError(message)
                    }
                }
            }
        }, cameraHandler)
    }

    private fun initPreviewSession() {
        imageReader = ImageReader
            .newInstance(previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 2)
            .apply { setOnImageAvailableListener(this@CameraUtils, imageReaderHandler) }

        val surface = cameraHost.provideTextureViewSurface()
        val captureRequest = getCaptureRequest(surface)

        camera.createCaptureSession(
            listOf(surface, imageReader.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@CameraUtils.session = session
                    session.setRepeatingRequest(captureRequest, null, cameraHandler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Camera ${camera.id} session configuration failed")
                }
            }, cameraHandler
        )
    }

    private fun getCaptureRequest(surface: Surface) =
        camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            .apply {
                addTarget(surface)
                addTarget(imageReader.surface)
            }.build()

    override fun onImageAvailable(reader: ImageReader?) {
        var image: Image? = null
        try {
            image = reader?.acquireLatestImage()
            image ?: return
            if (isProcessing) {
                image.close()
                return
            }
            isProcessing = true
            val rgbBytes = ImageUtils.convertYUVImageToARGB(image)
            Bitmap.createBitmap(rgbBytes, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888).let {
                bitmap = it
                cameraHost.onImageAvailable(it, orientation)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while acquiring image. Skipped.")
            ex.printStackTrace()
        } finally {
            image?.close()
        }
    }

    private fun getSensorOrientation(cameraId: String) =
        cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SENSOR_ORIENTATION)!!

    private fun getCameraId(): String? = cameraManager.run {
        cameraIdList.firstOrNull { id: String ->
            val characteristics = getCameraCharacteristics(id)
            val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK &&
                    capabilities?.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) ?: false
        }
    }

    private fun getSmallestValidOutputSize(cameraId: String): Size {
        val minSize = max(
            min(DESIRED_PREVIEW_SIZE.width, DESIRED_PREVIEW_SIZE.height),
            MINIMAL_VALID_PREVIEW_SIZE
        )

        return cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            .getOutputSizes(SurfaceHolder::class.java)
            .filter { it.width >= minSize && it.height >= minSize }
            .sortedBy { it.width * it.height }
            .also { Log.i(TAG, TextUtils.join("\n", it)) }
            .first()
    }

    private fun initBackgroundThreads() {
        cameraThread = HandlerThread("Camera Thread").apply { start() }
        cameraHandler = Handler(cameraThread!!.looper)
        imageReaderThread = HandlerThread("ImageReader Thread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
    }

    private fun destroyBackgroundThreads() {
        try {
            cameraHandler?.removeCallbacksAndMessages(null)
            cameraHandler = null
            cameraThread?.quitSafely()
            cameraThread?.join()
            cameraThread = null

            imageReaderHandler?.removeCallbacksAndMessages(null)
            imageReaderHandler = null
            imageReaderThread?.quitSafely()
            imageReaderThread?.join()
            imageReaderThread = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error while stopping background thread: ${e.stackTrace}")
        }
    }

    interface CameraEventListener {
        val cameraContext: Context
        fun onPreviewSizeSelected(size: Size)
        fun onError(text: String)
        fun onImageAvailable(bitmap: Bitmap, orientation: Int)
        fun provideTextureViewSurface(): Surface
    }
}
