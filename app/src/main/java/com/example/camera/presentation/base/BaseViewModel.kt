package com.example.camera.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel<T> : ViewModel() {

    private val _action = SingleLiveEvent<T>()
    private val compositeDisposable by lazy { CompositeDisposable() }

    val action: LiveData<T> get() = _action

    protected fun setAction(action: T) = _action.postValue(action)

    protected fun <T> Single<T>.subscribe(onSuccess: ((T) -> Unit)?, onError: (throwable: Throwable) -> Unit) =
        compositeDisposable.add(this.subscribe(onSuccess, onError))

    protected fun Completable.subscribe(onComplete: (() -> Unit)?, onError: (throwable: Throwable) -> Unit) =
        compositeDisposable.add(this.subscribe(onComplete, onError))

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
