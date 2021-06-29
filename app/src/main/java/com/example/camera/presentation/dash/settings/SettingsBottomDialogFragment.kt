package com.example.camera.presentation.dash.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.camera.databinding.DialogSetupBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsBottomDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogSetupBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogSetupBinding.inflate(inflater)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        binding.vm = viewModel
        return binding.root
    }



}