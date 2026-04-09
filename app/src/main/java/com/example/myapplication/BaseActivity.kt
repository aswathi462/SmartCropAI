package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    private var appliedLanguageCode: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appliedLanguageCode = LocaleManager.getSavedLanguage(this)
    }

    override fun onResume() {
        super.onResume()
        val latestLanguageCode = LocaleManager.getSavedLanguage(this)
        val currentApplied = appliedLanguageCode

        if (currentApplied != null && latestLanguageCode != currentApplied) {
            appliedLanguageCode = latestLanguageCode
            recreate()
            return
        }

        appliedLanguageCode = latestLanguageCode
    }
}
