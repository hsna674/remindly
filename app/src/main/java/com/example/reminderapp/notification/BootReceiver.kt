package com.example.reminderapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.example.reminderapp.data.ReminderDatabase
import com.example.reminderapp.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed, rescheduling notifications")

            // Use coroutine to handle async database operations
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val database = ReminderDatabase.getDatabase(context)
                    val settingsManager = SettingsManager(context)
                    val notificationScheduler = NotificationScheduler(context)

                    // Get all reminders and notification settings
                    database.reminderDao().getAllReminders().collect { reminders ->
                        val notificationSettings = settingsManager.notificationSettings.value

                        // Reschedule notifications for all reminders
                        reminders.forEach { reminder ->
                            notificationScheduler.scheduleNotificationsForReminder(reminder, notificationSettings)
                        }

                        Log.d("BootReceiver", "Rescheduled notifications for ${reminders.size} reminders")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling notifications after boot: ${e.message}")
                }
            }
        }
    }
}
