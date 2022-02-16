package com.example.camera.processing

import android.util.Log
import com.example.camera.processing.detection.MAX_PROCESSING_TIME_SECONDS
import com.example.camera.processing.detection.TAG
import io.reactivex.Single
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.util.concurrent.TimeUnit

class SocketManager(
    private val serverAddress: String,
    private val initQuery: String
) {

    private lateinit var socket: Socket
    private val isConnected get() = socket.connected()

    init {
        setupSocket()
    }

    private fun setupSocket() {
        try {
            socket = IO.socket(
                serverAddress,
                IO.Options.builder()
                    .setTransports(arrayOf(WebSocket.NAME))
                    .setQuery(initQuery)
                    .build()
            )
            socket.on(Socket.EVENT_CONNECT_ERROR) { Log.e(TAG, "ERROR: ${it[0]}") }
            socket.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket initialization failed")
        }
    }

    fun dispose() {
        if (::socket.isInitialized) {
            socket.close()
        }
    }

    fun emitEvent(event: String, vararg args: Any): Single<Array<Any>> =
        if (isConnected) {
            Single.create<Array<Any>> { emitter ->
                socket.emit(event, args) { result ->
                    result?.let { emitter.onSuccess(it) }
                        ?: emitter.onError(RuntimeException("Socket returned null for result"))
                }
            }.timeout(MAX_PROCESSING_TIME_SECONDS, TimeUnit.SECONDS)
        } else {
            Single.error(SocketDisconnectedException())
        }
}
