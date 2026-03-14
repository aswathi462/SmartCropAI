package com.example.myapplication
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Switch this to activity_main for now just so it doesn't crash the build
        setContentView(R.layout.activity_main)
    }
}