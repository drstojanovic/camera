package com.example.camera.utils

import android.graphics.SurfaceTexture
import android.view.TextureView

fun interface OnSurfaceTextureAvailableListener : TextureView.SurfaceTextureListener {

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = false

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) =
        onSurfaceTextureAvailable()

    fun onSurfaceTextureAvailable()
}
