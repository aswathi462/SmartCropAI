package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Find views using the IDs from your XML
        val signUpButton = findViewById<Button>(R.id.btnSignUp)
        val loginLink = findViewById<TextView>(R.id.tvLoginLink)

        signUpButton.setOnClickListener {
            // Logic for when they click Sign Up
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            finish() // Goes back to the previous screen
        }

        loginLink.setOnClickListener {
            // Goes back to Login if they already have an account
            finish()
        }
    }
}
