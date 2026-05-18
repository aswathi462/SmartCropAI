package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 1. Initialize Authentication and your free Singapore Realtime Database node
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance("https://smartcropai-265bd-default-rtdb.asia-southeast1.firebasedatabase.app/")

        // Match the layout IDs
        val name = findViewById<TextInputEditText>(R.id.etName)
        val email = findViewById<TextInputEditText>(R.id.etEmail)
        val password = findViewById<TextInputEditText>(R.id.etPassword)

        val signUpButton = findViewById<Button>(R.id.btnSignUp)
        val loginLink = findViewById<TextView>(R.id.tvLoginLink)

        signUpButton.setOnClickListener {

            val nameText = name.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            // 2. Multi-language field check validation
            if (nameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Authenticate and build the Firebase account
            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->

                    if (!task.isSuccessful) {
                        Toast.makeText(
                            this,
                            task.exception?.message ?: "Signup failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addOnCompleteListener
                    }

                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Map the username and email parameters together
                    val userMap = hashMapOf(
                        "username" to nameText,
                        "email" to emailText
                    )

                    // 4. Write data profile to the free Realtime Database structural node
                    dbRef.getReference("users")
                        .child(userId)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, getString(R.string.signup_success), Toast.LENGTH_SHORT).show()

                            // Navigate seamlessly to the main Dashboard screen
                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            // Catches any local processing errors or connectivity drops
                            Toast.makeText(this, "Database Save Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
        }

        loginLink.setOnClickListener {
            finish()
        }
    }
}