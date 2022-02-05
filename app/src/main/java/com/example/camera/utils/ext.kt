package com.example.camera.utils

import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.reactivex.Single

val Any.TAG
    get() = this::class.java.simpleName

fun View.spToPx(sp: Int): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics)

fun Paint.withColor(color: Int) =
    this.apply { setColor(color) }

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (T) -> Unit) {
    liveData.observe(this) { action.invoke(it) }
}

fun <T, R> Single<List<T>>.mapItems(mapper: ((T) -> R)): Single<List<R>> =
    this.map { list -> list.map { mapper.invoke(it) } }

fun <T> Single<List<T>>.filterItems(predicate: ((T) -> Boolean)): Single<List<T>> =
    this.map { list -> list.filter(predicate) }
