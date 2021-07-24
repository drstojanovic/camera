package com.example.camera.presentation.setup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.camera.R
import com.example.camera.databinding.ActivitySetupBinding
import com.example.camera.presentation.main.MainActivity

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var viewModel: SetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setVariables()
        setObservers()
    }


    private fun setObservers() {
        viewModel.action.observe(this) { action ->
            if (action == SetupViewModel.SetupAction.PROCEED)
                startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setVariables() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setup)
        viewModel = ViewModelProvider(this).get(SetupViewModel::class.java)
        binding.vm = viewModel
    }
}
