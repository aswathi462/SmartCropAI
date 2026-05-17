package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The modern Android framework handles everything behind the scenes.
        // Leaving this base class empty ensures all extending activities (Login, SignUp, etc.)
        // inherit the global, native language preference context automatically!
    }
}