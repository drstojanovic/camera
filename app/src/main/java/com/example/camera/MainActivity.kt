package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.camera.tflite.Classifier
import com.example.camera.utils.AutoFitTextureView
import com.example.camera.utils.convertYUVImageToARGB
import com.example.camera.utils.saveBitmap
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.max
import kotlin.math.min

private const val TAG = "CameraTAG"
private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
private val SIZE_1080P = SmartSize(1920, 1080)
private val DESIRED_PREVIEW_SIZE = Size(640, 480)
private const val MINIMAL_VALID_PREVIEW_SIZE = 320  // empiric value
private const val RESULT_FORMAT = "%s %.2f"

class MainActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener {

    class CameraHandler(looper: Looper) : Handler(looper)
    class CameraThread(name: String) : HandlerThread(name)
    class ImageReaderHandler(looper: Looper) : Handler(looper)
    class ImageReaderThread(name: String) : HandlerThread(name)

    private val cameraThread = CameraThread("Camera Thread").apply { start() }
    private val cameraHandler = CameraHandler(cameraThread.looper)
    private val imageReaderThread = ImageReaderThread("ImageReader Thread").apply { start() }
    private val imageReaderHandler = ImageReaderHandler(imageReaderThread.looper)
    private val imageProcessor by lazy { ImageProcessor(this) }
    private val compositeDisposable = CompositeDisposable()

    private var orientation: Int = 270
    private lateinit var camera: CameraDevice
    private lateinit var imageReader: ImageReader       // IMPORTANT: imageReader as class field due to exceptions thrown in middle of preview
    private lateinit var previewSize: Size
    private lateinit var textureView: AutoFitTextureView
    private lateinit var txtResult1: TextView
    private lateinit var txtResult2: TextView
    private lateinit var txtResult3: TextView
    private val cameraManager: CameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkForPermissionsAndInitViews()
        txtResult1 = findViewById(R.id.txt_result_1)
        txtResult2 = findViewById(R.id.txt_result_2)
        txtResult3 = findViewById(R.id.txt_result_3)
    }

    // region Tear down methods
    override fun onStop() {
        super.onStop()
        try {
            camera.close()
            Log.d(TAG, "Camera closed")
        } catch (exc: Throwable) {
            Log.e(TAG, "Error while closing camera: " + exc.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageReaderThread.quitSafely()
        Log.d(TAG, "ImageReader thread closed")
        cameraThread.quitSafely()
        Log.d(TAG, "Camera thread closed")
        compositeDisposable.dispose()
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

    // region Camera Initialization
    private fun initViews() {
        textureView = findViewById(R.id.texture_view)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) = setupCamera()
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = false
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit
        }
    }

    private fun setupCamera() {
        val cameraId = getCameraId()
        if (cameraId == null) {
            Toast.makeText(this, "No cameras available!", LENGTH_SHORT).show()
            return
        }

        orientation = windowManager.defaultDisplay.rotation -
                cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        Log.d(TAG, "Orientation: $orientation")
        previewSize = getSmallestValidOutputSize(cameraId)
        Log.d(TAG, "Surface View preview size: ${textureView.width} x ${textureView.height}")
        Log.d(TAG, "Selected preview size: $previewSize")
        textureView.setAspectRatio(previewSize.height, previewSize.width)
        Log.d(TAG, "Surface View preview size after applying values: ${textureView.width} x ${textureView.height}")
        textureView.post { openCamera(cameraId) }    // IMPORTANT - post (make sure that size is set first and then executed rest of code)
    }

    private fun initPreviewSession() {
        imageReader = ImageReader.newInstance(
            previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 2
        ).apply { setOnImageAvailableListener(this@MainActivity, imageReaderHandler) }

        textureView.surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val surface = Surface(textureView.surfaceTexture)
        val captureRequestBuilder =
            camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
                addTarget(imageReader.surface)
            }

        camera.createCaptureSession(
            listOf(surface, imageReader.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Camera ${camera.id} session configuration failed")
                }
            }, cameraHandler
        )
    }

    //endregion

    private var isProcessing = false
    private var bitmap: Bitmap? = null
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
            val rgbBytes = convertYUVImageToARGB(image, previewSize.width, previewSize.height)
            bitmap = Bitmap.createBitmap(rgbBytes, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888)
            processImage(bitmap)
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while acquiring image. Skipped.")
            ex.printStackTrace()
        } finally {
            image?.close()
        }
    }

    private fun processImage(image: Bitmap?) {
        image ?: return
        compositeDisposable.add(
            imageProcessor.processImage(image, orientation)
                .subscribe(
                    { result ->
                        txtResult1.text = RESULT_FORMAT.format(result[0].title, result[0].confidence * 100)
                        txtResult2.text = RESULT_FORMAT.format(result[1].title, result[1].confidence * 100)
                        txtResult3.text = RESULT_FORMAT.format(result[2].title, result[2].confidence * 100)
                        isProcessing = false
                    },
                    { throwable ->
                        Log.e(TAG, throwable.stackTraceToString())
                        isProcessing = false
                    }
                )
        )
    }

    // region Helper Methods
    @SuppressLint("MissingPermission")
    private fun openCamera(cameraId: String) {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                Log.d(TAG, "Camera $cameraId is open")
                camera = cameraDevice
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

    private fun getHDPreviewOutputSize(cameraId: String): Size {
        val screenSize = getDisplaySize()
        val hdScreen = screenSize.long >= SIZE_1080P.long || screenSize.short >= SIZE_1080P.short
        val maxAllowedSize = if (hdScreen) SIZE_1080P else screenSize

        return cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            .getOutputSizes(SurfaceHolder::class.java)
            .also { Log.i(TAG, TextUtils.join("\n", it)) }
            .sortedWith(compareBy { it.height * it.width })
            .map { SmartSize(it.width, it.height) }
            .reversed()
            .first { it.long <= maxAllowedSize.long && it.short <= maxAllowedSize.short }.size
    }

    private fun getDisplaySize() = Point().let {
        textureView.display.getRealSize(it)
        SmartSize(it.x, it.y)
    }

    private fun getCameraId(): String? = cameraManager.run {
        cameraIdList.firstOrNull { id: String ->
            val characteristics = getCameraCharacteristics(id)
            val capabilities =
                characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK &&
                    (capabilities?.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)
                        ?: false)

        }
    }

    fun saveImage(view: View) {
        saveBitmap(this, bitmap, -orientation)
        Toast.makeText(this, "Image Saved", LENGTH_SHORT).show()
    }

    // endregion

}
