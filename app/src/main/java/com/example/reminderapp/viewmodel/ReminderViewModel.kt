package com.example.reminderapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.reminderapp.data.*
import com.example.reminderapp.notification.NotificationScheduler

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ReminderDatabase.getDatabase(application)
    private val repository = ReminderRepository(database.reminderDao())
    private val settingsManager = SettingsManager(application)
    private val notificationScheduler = NotificationScheduler(application)

    // Selected date state
    private val _selectedDate = MutableStateFlow(loadSelectedDate())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // All reminders from database
    val allReminders: StateFlow<List<Reminder>> = repository.getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // School classes from settings
    val schoolClasses: StateFlow<List<SchoolClass>> = settingsManager.schoolClasses

    // Notification settings from settings
    val notificationSettings: StateFlow<List<NotificationSetting>> = settingsManager.notificationSettings

    // Dark mode setting from settings
    val isDarkMode: StateFlow<Boolean> = settingsManager.isDarkMode

    // Get reminders for selected date
    val selectedDateReminders: StateFlow<List<Reminder>> = combine(
        selectedDate,
        allReminders
    ) { date, reminders ->
        reminders.filter { it.date == date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun loadSelectedDate(): LocalDate {
        val savedDate = settingsManager.getSelectedDate()
        return if (savedDate != null) {
            try {
                LocalDate.parse(savedDate)
            } catch (e: Exception) {
                LocalDate.now()
            }
        } else {
            LocalDate.now()
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        settingsManager.saveSelectedDate(date.toString())
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                // Basic validation
                if (reminder.name.isBlank()) {
                    Log.w("ReminderViewModel", "Cannot add reminder with empty name")
                    return@launch
                }

                repository.insertReminder(reminder)
                // Schedule notifications for the new reminder
                val settings = notificationSettings.value
                notificationScheduler.scheduleNotificationsForReminder(reminder, settings)
                Log.d("ReminderViewModel", "Successfully added reminder: ${reminder.name}")
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error adding reminder: ${e.message}")
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                // Cancel notifications before deleting
                val settings = notificationSettings.value
                notificationScheduler.cancelNotificationsForReminder(reminder.id, settings)
                repository.deleteReminder(reminder)
                Log.d("ReminderViewModel", "Successfully deleted reminder: ${reminder.name}")
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error deleting reminder: ${e.message}")
            }
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                // Basic validation
                if (reminder.name.isBlank()) {
                    Log.w("ReminderViewModel", "Cannot update reminder with empty name")
                    return@launch
                }

                // Cancel old notifications and schedule new ones
                val settings = notificationSettings.value
                notificationScheduler.cancelNotificationsForReminder(reminder.id, settings)
                repository.updateReminder(reminder)
                notificationScheduler.scheduleNotificationsForReminder(reminder, settings)
                Log.d("ReminderViewModel", "Successfully updated reminder: ${reminder.name}")
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error updating reminder: ${e.message}")
            }
        }
    }

    fun updateSchoolClasses(classes: List<SchoolClass>) {
        settingsManager.saveClasses(classes)
    }

    fun updateNotificationSettings(settings: List<NotificationSetting>) {
        settingsManager.saveNotificationSettings(settings)
        // Reschedule all notifications with new settings
        viewModelScope.launch {
            rescheduleAllNotifications()
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        settingsManager.setDarkMode(enabled)
    }

    private suspend fun rescheduleAllNotifications() {
        val reminders = allReminders.value
        val settings = notificationSettings.value

        // Cancel all existing notifications
        reminders.forEach { reminder ->
            notificationScheduler.cancelNotificationsForReminder(reminder.id, settings)
        }

        // Schedule new notifications with updated settings
        reminders.forEach { reminder ->
            notificationScheduler.scheduleNotificationsForReminder(reminder, settings)
        }
    }

    fun getRemindersForDate(date: LocalDate): StateFlow<List<Reminder>> {
        return repository.getRemindersByDate(date)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}
