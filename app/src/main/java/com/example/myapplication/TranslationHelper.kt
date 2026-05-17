package com.example.myapplication

import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation

object TranslationHelper {

    fun translate(text: String, targetLang: String, callback: (String) -> Unit) {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage("en")
            .setTargetLanguage(targetLang)
            .build()

        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translated ->
                        callback(translated)
                    }
                    .addOnFailureListener {
                        callback(text) // fallback: show English
                    }
            }
            .addOnFailureListener {
                callback(text) // fallback
            }
    }
}