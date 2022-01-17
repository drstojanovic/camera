package com.example.camera.presentation.base

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.example.camera.BR

abstract class BaseActivity<BindingT : ViewDataBinding, ViewModelT : BaseViewModel<*>> : AppCompatActivity() {

    protected lateinit var binding: BindingT
    protected lateinit var viewModel: ViewModelT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBindingAndViewModel()
    }

    abstract fun provideLayoutId(): Int

    abstract fun provideViewModelClass(): Class<ViewModelT>

    private fun initBindingAndViewModel() {
        viewModel = ViewModelProvider(this).get(provideViewModelClass())
        binding = DataBindingUtil.setContentView(this, provideLayoutId())
        binding.lifecycleOwner = this
        binding.setVariable(BR.vm, viewModel)
    }

    protected fun showToast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
        Toast.makeText(this, message, duration).show()

    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
        Toast.makeText(this, message, duration).show()
}