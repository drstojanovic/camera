package com.example.camera.detection

import android.content.res.AssetManager
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object ModelUtils {

    fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer =
        assets.openFd(modelFilename).let { fileDescriptor ->
            FileInputStream(fileDescriptor.fileDescriptor)
                .channel
                .map(
                    FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.startOffset,
                    fileDescriptor.declaredLength
                )
        }

}
