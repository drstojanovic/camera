package com.example.camera.processing.detection

import androidx.lifecycle.LifecycleObserver
import com.example.camera.processing.ImagePreprocessor
import com.example.camera.processing.Settings
import com.example.camera.processing.SocketManager
import com.example.camera.utils.EVENT_IMAGE
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Single

/**
 * Socket.io docs
 * https://socketio.github.io/socket.io-client-java
 */
class RemoteObjectDetector(
    settings: Settings
) : ObjectDetector(settings), LifecycleObserver {

    private val socketManager =
        SocketManager(settings.serverAddressFull, settings.toQuery())
    private val recognitionAdapter: JsonAdapter<List<RecognitionRaw>> =
        Moshi.Builder().build().adapter(Types.newParameterizedType(List::class.java, RecognitionRaw::class.java))

    override fun dispose() = socketManager.dispose()

    override fun detectObjects(imageBytes: ByteArray): Single<List<Recognition>> =
        socketManager.emitEvent(EVENT_IMAGE, ImagePreprocessor.encodeBytes(imageBytes))
            .map { result ->
                if (result.isNotEmpty() && result[0] != "") {
                    recognitionAdapter.fromJson(result[0].toString())?.let { recs ->
                        recs.mapIndexed { index, r -> r.toRecognition(index) }
                    }
                } else listOf()
            }
}
