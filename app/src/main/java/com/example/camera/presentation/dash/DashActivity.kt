package com.example.camera.presentation.dash

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.camera.R
import com.example.camera.presentation.main.MainActivity

class DashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)

        findViewById<Button>(R.id.btn_proceed).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
