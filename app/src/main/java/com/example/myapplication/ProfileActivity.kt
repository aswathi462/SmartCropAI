package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton

class ProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val currentUser = UserStore.getCurrentUser(this)
        val profileName = findViewById<TextView>(R.id.tvProfileName)
        val profileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val logoutButton = findViewById<MaterialButton>(R.id.btnLogoutProfile)

        profileName.text = currentUser?.first ?: getString(R.string.default_profile_name)
        profileEmail.text = currentUser?.second ?: getString(R.string.default_profile_email)

        findViewById<ImageButton>(R.id.btnBackProfile).setOnClickListener {
            finish()
        }

        LanguageSelectorHelper.bind(this, R.id.languageAutocompleteProfile) {
            Toast.makeText(this, getString(R.string.language_updated), Toast.LENGTH_SHORT).show()
            recreate()
        }

        logoutButton.setOnClickListener {
            UserStore.clearCurrentUser(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}