package com.example.camera.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max

// This value is 2 ^ 18 - 1, and is used to clamp the RGB values before their ranges
// are normalized to eight bits.
const val K_MAX_CHANNEL_VALUE = 262143

//todo: check if image.width == width and if so remove parameters
fun convertYUVImageToARGB(image: Image, width: Int, height: Int): IntArray {
    getYUVBytes(image).let { yuvBytes ->
        val yRowStride = image.planes[0].rowStride
        val uvRowStride = image.planes[1].rowStride
        val uvPixelStride = image.planes[1].pixelStride

        return convertYUV420ToARGB8888(
            yuvBytes[0], yuvBytes[1], yuvBytes[2],
            width, height, yRowStride, uvRowStride, uvPixelStride
        )
    }
}

private fun getYUVBytes(image: Image): Array<ByteArray> =
    Array(3) { byteArrayOf() }.also { byteArray ->
        image.planes.forEachIndexed { index, plane ->
            plane.buffer.let { buffer ->
                ByteArray(buffer.capacity())
                    .apply { buffer.get(this) }
                    .also { byteArray[index] = it }
            }
        }
    }

private fun convertYUV420ToARGB8888(
    yData: ByteArray,
    uData: ByteArray,
    vData: ByteArray,
    width: Int,
    height: Int,
    yRowStride: Int,
    uvRowStride: Int,
    uvPixelStride: Int
): IntArray {
    val result = IntArray(width * height)
    var yp = 0
    for (j in 0 until height) {
        val pY = yRowStride * j
        val pUV = uvRowStride * (j shr 1)
        for (i in 0 until width) {
            val uvOffset = pUV + (i shr 1) * uvPixelStride
            result[yp++] = yuv2rgb(
                0xff and yData[pY + i].toInt(),
                0xff and uData[uvOffset].toInt(),
                0xff and vData[uvOffset].toInt()
            )
        }
    }
    return result
}

private fun yuv2rgb(yIn: Int, uIn: Int, vIn: Int): Int {
    // Adjust and check YUV values
    var y = yIn
    var u = uIn
    var v = vIn

    y = max(y - 16, 0)
    u -= 128
    v -= 128

    // This is the floating point equivalent. We do the conversion in integer
    // because some Android devices do not have floating point in hardware.
    // nR = (int)(1.164 * nY + 2.018 * nU);
    // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
    // nB = (int)(1.164 * nY + 1.596 * nV);
    val y1192 = 1192 * y
    var r = y1192 + 1634 * v
    var g = y1192 - 833 * v - 400 * u
    var b = y1192 + 2066 * u

    // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
    r = if (r > K_MAX_CHANNEL_VALUE) K_MAX_CHANNEL_VALUE else if (r < 0) 0 else r
    g = if (g > K_MAX_CHANNEL_VALUE) K_MAX_CHANNEL_VALUE else if (g < 0) 0 else g
    b = if (b > K_MAX_CHANNEL_VALUE) K_MAX_CHANNEL_VALUE else if (b < 0) 0 else b
    return -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
}

fun saveBitmap(context: Context, bitmap: Bitmap?, filename: String = "test.png") {
    bitmap ?: return
    var outputStream: OutputStream? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }.let { contentValues ->
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }?.let { imageUri ->
            outputStream = context.contentResolver.openOutputStream(imageUri)
        }
    } else {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename)
        outputStream = FileOutputStream(file)
    }

    try {
        outputStream?.run {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            flush()
            close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}