package com.example.reminderapp.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.gson.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.core.content.edit

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("reminder_app_settings", Context.MODE_PRIVATE)

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME))
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer<LocalTime> { json, _, _ ->
            LocalTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_TIME)
        })
        .create()

    private val _schoolClasses = MutableStateFlow(loadClasses())
    val schoolClasses: StateFlow<List<SchoolClass>> = _schoolClasses.asStateFlow()

    private val _notificationSettings = MutableStateFlow(loadNotificationSettings())
    val notificationSettings: StateFlow<List<NotificationSetting>> = _notificationSettings.asStateFlow()

    // Dark mode preference management
    private val _isDarkMode = MutableStateFlow(loadDarkModePreference())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private fun loadDarkModePreference(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false) // Default to light mode
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean("dark_mode", enabled)
        }
        _isDarkMode.value = enabled
    }

    private fun loadClasses(): List<SchoolClass> {
        val classesJson = sharedPreferences.getString("school_classes", null)
        return if (classesJson != null) {
            try {
                val type = object : TypeToken<List<SchoolClass>>() {}.type
                gson.fromJson(classesJson, type)
            } catch (_: Exception) {
                SchoolClasses.defaultClasses
            }
        } else {
            SchoolClasses.defaultClasses
        }
    }

    private fun loadNotificationSettings(): List<NotificationSetting> {
        val settingsJson = sharedPreferences.getString("notification_settings", null)
        return if (settingsJson != null) {
            try {
                val type = object : TypeToken<List<NotificationSetting>>() {}.type
                gson.fromJson(settingsJson, type)
            } catch (_: Exception) {
                DefaultNotificationSettings.defaultSettings
            }
        } else {
            DefaultNotificationSettings.defaultSettings
        }
    }

    fun saveClasses(classes: List<SchoolClass>) {
        val classesJson = gson.toJson(classes)
        sharedPreferences.edit {
            putString("school_classes", classesJson)
        }
        _schoolClasses.value = classes
    }

    fun saveNotificationSettings(settings: List<NotificationSetting>) {
        val settingsJson = gson.toJson(settings)
        sharedPreferences.edit {
            putString("notification_settings", settingsJson)
        }
        _notificationSettings.value = settings
    }

    fun getSelectedDate(): String? {
        return sharedPreferences.getString("selected_date", null)
    }

    fun saveSelectedDate(date: String) {
        sharedPreferences.edit {
            putString("selected_date", date)
        }
    }
}
