package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 👉 Correct layout
        setContentView(R.layout.activity_profile)

        val panel = findViewById<LinearLayout>(R.id.slidePanel)
        val profile = findViewById<ImageView>(R.id.imgProfile)

        var open = false

        profile.setOnClickListener {
            if (!open) {
                panel.animate().translationX(0f).setDuration(300).start()
            } else {
                panel.animate().translationX(-250f).setDuration(300).start()
            }
            open = !open
        }

        // (Optional) set text later if you fetch from Firebase
        val name = findViewById<TextView>(R.id.tvName)
        val email = findViewById<TextView>(R.id.tvEmail)
    }
}