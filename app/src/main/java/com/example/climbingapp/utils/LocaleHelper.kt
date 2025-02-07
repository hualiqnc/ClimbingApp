package com.example.climbingapp.utils

import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import java.util.Locale

class LocaleHelper(private val context: Context) {
    companion object {
        const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    }

    fun setLocale(language: String): Context {
        persist(language)
        return updateResources(language)
    }

    private fun persist(language: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putString(SELECTED_LANGUAGE, language).apply()
    }

    private fun updateResources(language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    fun getLanguage(): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }
} 