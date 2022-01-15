package com.example.camera.utils

import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

val Any.TAG
    get() = this::class.java.simpleName

fun View.spToPx(sp: Int): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics)

fun Paint.withColor(color: Int) =
    this.apply { setColor(color) }

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (T) -> Unit) {
    liveData.observe(this) { action.invoke(it) }
}
