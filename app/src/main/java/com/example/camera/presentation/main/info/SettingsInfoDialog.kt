package com.example.camera.presentation.main.info

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.camera.R
import com.example.camera.databinding.DialogSettingsInfoBinding

class SettingsInfoDialog(
    context: Context,
    private val settingsInfo: SettingsInfo
) : Dialog(context, R.style.SettingsInfoDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        DialogSettingsInfoBinding.inflate(layoutInflater).run {
            setContentView(this.root)
            settings = settingsInfo
            btnClose.setOnClickListener { dismiss() }
        }
    }
}
