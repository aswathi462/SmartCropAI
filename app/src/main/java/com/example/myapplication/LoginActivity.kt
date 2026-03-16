package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        LanguageSelectorHelper.bind(this, R.id.languageAutocomplete)

        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val signupText = findViewById<TextView>(R.id.signupText)

        intent.getStringExtra("prefill_email")?.let { email ->
            username.setText(email)
        }

        loginButton.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString()
            val registeredUser = UserStore.getUser(this)
            val isValidRegisteredUser = registeredUser != null &&
                user.equals(registeredUser.email, ignoreCase = true) &&
                pass == registeredUser.password

            val isValidDefaultAdmin = user == "admin" && pass == "1234"

            if (isValidRegisteredUser || isValidDefaultAdmin) {
                if (isValidRegisteredUser && registeredUser != null) {
                    UserStore.saveCurrentUser(this, registeredUser.fullName, registeredUser.email)
                } else {
                    UserStore.saveCurrentUser(this, "Admin", "admin")
                }

                Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()

                // ⭐ Navigate to DashboardActivity
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)

                finish() // optional: prevents going back to login screen
            } else {
                Toast.makeText(this, getString(R.string.login_invalid), Toast.LENGTH_SHORT).show()
            }
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}