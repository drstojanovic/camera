package com.example.camera.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.camera.presentation.mode.ModeActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(ModeActivity.createIntent())
        finish()
    }
}
