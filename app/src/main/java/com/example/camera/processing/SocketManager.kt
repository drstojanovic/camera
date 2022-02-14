package com.example.camera.processing

import android.util.Log
import com.example.camera.processing.detection.MAX_PROCESSING_TIME_MILLIS
import com.example.camera.processing.detection.TAG
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

    suspend fun emitEvent(event: String, vararg args: Any): Array<Any> = withContext(Dispatchers.IO) {
        val deferred = async { execute(event, args) }
        withTimeout(MAX_PROCESSING_TIME_MILLIS) { deferred.await() }
    }

    private suspend fun execute(event: String, vararg args: Any): Array<Any> =
        suspendCoroutine { continuation ->
            if (isConnected) {
                socket.emit(event, args) { result ->
                    result
                        ?.let { continuation.resume(it) }
                        ?: continuation.resumeWithException(RuntimeException("Socket returned null for result"))
                }
            } else {
                continuation.resumeWithException(SocketDisconnectedException())
            }
        }
}
