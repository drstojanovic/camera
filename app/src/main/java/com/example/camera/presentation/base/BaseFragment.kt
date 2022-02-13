package com.example.camera.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.camera.BR

abstract class BaseFragment<BindingT : ViewDataBinding, ViewModelT : BaseViewModel<*>> : Fragment() {

    protected lateinit var binding: BindingT
    protected lateinit var viewModel: ViewModelT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(provideViewModelClass())
        binding = DataBindingUtil.inflate(layoutInflater, provideLayoutId(), container, false)
        binding.lifecycleOwner = this
        binding.setVariable(BR.vm, viewModel)
        return binding.root
    }

    abstract fun provideLayoutId(): Int

    abstract fun provideViewModelClass(): Class<ViewModelT>

    protected fun showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
        context?.let { Toast.makeText(it, message, duration).show() }

    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
        context?.let { Toast.makeText(it, message, duration).show() }
}