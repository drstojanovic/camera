package com.example.camera.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.camera.presentation.setup.SetupActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(SetupActivity.createIntent())
        finish()
    }
}
