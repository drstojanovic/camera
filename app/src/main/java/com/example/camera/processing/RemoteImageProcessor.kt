package com.example.camera.processing

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.example.camera.detection.Recognition
import com.example.camera.detection.RecognitionRaw
import com.example.camera.detection.toRecognition
import com.example.camera.utils.EVENT_IMAGE
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Single
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.io.ByteArrayOutputStream

class RemoteImageProcessor(
    private val settings: Settings
) : ImageProcessor(), LifecycleObserver {

    private lateinit var socket: Socket
    private val recognitionAdapter: JsonAdapter<List<RecognitionRaw>> =
        Moshi.Builder().build()
            .adapter(Types.newParameterizedType(List::class.java, RecognitionRaw::class.java))

    init {
        setupSocket()
    }

    private fun setupSocket() {
        try {
            socket = IO.socket(
                settings.serverAddressFull,
                IO.Options.builder()
                    .setTransports(arrayOf(WebSocket.NAME))
                    .setQuery(settings.toQuery())
                    .build()
            )
            socket.on(Socket.EVENT_CONNECT_ERROR) { Log.e(TAG, "ERROR: ${it[0]}") }
            socket.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket initialization failed")
        }
    }

    override fun dispose() {
        socket.close()
    }

    override fun process(image: Bitmap): Single<List<Recognition>> {
        if (!socket.connected()) {
            return Single.error(IllegalStateException("Socket disconnected"))
        }

        var recognitions = listOf<Recognition>()
        val byteArrayStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayStream)
        val encodedBytes = Base64.encode(byteArrayStream.toByteArray(), Base64.NO_WRAP)

        return Single.create { emitter ->
            socket.emit(EVENT_IMAGE, encodedBytes, Ack { result ->
                if (result.isNotEmpty() && result[0] != "") {
                    recognitionAdapter.fromJson(result[0].toString())?.let { recs ->
                        recognitions = recs.mapIndexed { index, r -> r.toRecognition(index) }
                    }
                }
                emitter.onSuccess(recognitions)
            })
        }
    }
}
