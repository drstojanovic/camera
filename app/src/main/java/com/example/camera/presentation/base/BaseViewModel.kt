package com.example.camera.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<T> : ViewModel() {

    private val _action = SingleLiveEvent<T>()

    val action: LiveData<T> get() = _action

    protected fun setAction(action: T) = _action.postValue(action)

}
