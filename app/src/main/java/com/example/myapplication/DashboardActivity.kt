package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DashboardActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Manage Dark Mode Preference State Cleanly
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase Authentication and the Realtime Database Cluster
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance("https://smartcropai-265bd-default-rtdb.asia-southeast1.firebasedatabase.app/")

        // -------- Drawer Layout Panel Setup --------
        drawerLayout = findViewById(R.id.drawerLayout)

        val btnProfile = findViewById<LinearLayout>(R.id.btnProfileSettings)
        btnProfile.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // -------- Profile Drawer UI Element Bindings --------
        val imgProfile = findViewById<ShapeableImageView>(R.id.imgProfile)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvSubName = findViewById<TextView>(R.id.tvSubName) // Binding the standard subtitle tag if present
        //val btnAboutUs = findViewById<MaterialButton>(R.id.btnAboutUs)
        val btnSelectLanguage = findViewById<MaterialButton>(R.id.btnSelectLanguage)
        val themeSwitch = findViewById<MaterialSwitch>(R.id.themeSwitch)

        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("DarkMode", isChecked)
            editor.apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // -------- FETCH USERNAME DYNAMICALLY FROM REALTIME DATABASE WITH LOCALE FORMATTING --------
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            dbRef.getReference("users").child(currentUserId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val dbUsername = snapshot.child("username").value as? String ?: ""
                        if (dbUsername.isNotEmpty() && dbUsername != "null") {
                            // FIX: Uses localized String resource argument injections safely
                            tvName.text = getString(R.string.welcome_user, dbUsername)
                        } else {
                            tvName.text = getString(R.string.welcome_user, getString(R.string.select_option_get_started))
                        }
                    }
                }
                .addOnFailureListener {
                    // Localized offline safe fallback display
                    tvName.text = getString(R.string.welcome_user, getString(R.string.select_option_get_started))
                }
        }

        // -------- NATIVE MULTI-LANGUAGE CHOOSER DIALOG ENGINE --------
        btnSelectLanguage.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)

            val languages = arrayOf("English", "മലയാളം (Malayalam)", "हिन्दी (Hindi)")
            val langCodes = arrayOf("en", "ml", "hi")

            val currentLang = LocaleManager.getSavedLanguage(this)
            val checkedItem = langCodes.indexOf(currentLang).coerceAtLeast(0)

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.change_language)) // Localized dialog menu header frame tracking
                .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                    val selectedLangCode = langCodes[which]

                    LocaleManager.saveLanguage(this, selectedLangCode)

                    val appLocale = LocaleListCompat.forLanguageTags(selectedLangCode)
                    AppCompatDelegate.setApplicationLocales(appLocale)

                    dialog.dismiss()

                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }


        val btnLogoutMenu = findViewById<MaterialButton>(R.id.btnLogout)
        btnLogoutMenu.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // -------- Main Dashboard Navigation Cards --------
        val cardDisease = findViewById<MaterialCardView>(R.id.cardDisease)
        cardDisease.setOnClickListener {
            val intent = Intent(this, DiseaseDetectionActivity::class.java)
            startActivity(intent)
        }

        val cardYield = findViewById<MaterialCardView>(R.id.cardYield)
        cardYield.setOnClickListener {
            val intent = Intent(this, YieldPredictionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}