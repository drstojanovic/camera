package com.example.camera.utils

import android.graphics.Paint
import android.os.Build
import android.util.TypedValue
import android.view.Display
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import java.lang.Exception

val Any.TAG: String
    get() = this::class.java.simpleName

val Fragment.displayCompat: Display?
    get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        @Suppress("DEPRECATION")
        this.activity?.windowManager?.defaultDisplay
    } else {
        this.activity?.display
    }

fun View.spToPx(sp: Int): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics)

fun Paint.withColor(color: Int) =
    this.apply { setColor(color) }

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (T) -> Unit) {
    liveData.observe(this) { action.invoke(it) }
}

suspend fun <T> tryToExecute(block: suspend () -> T): Result<T> =
    try {
        Result.success(block.invoke())
    } catch (e: Exception) {
        Result.failure(e)
    }

suspend fun <T> Result<T>.collect(onSuccess: (T?) -> Unit, onError: (e: Throwable) -> Unit) =
    if (this.isSuccess) {
        onSuccess.invoke(this.getOrNull())
    } else {
        onError.invoke(this.exceptionOrNull()!!)
    }