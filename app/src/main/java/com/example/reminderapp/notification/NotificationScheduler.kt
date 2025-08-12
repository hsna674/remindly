package com.example.reminderapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.data.NotificationSetting
import java.time.LocalDateTime
import java.time.ZoneId

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotificationsForReminder(reminder: Reminder, notificationSettings: List<NotificationSetting>) {
        notificationSettings.filter { it.isEnabled }.forEach { setting ->
            scheduleNotification(reminder, setting)
        }
    }

    private fun scheduleNotification(reminder: Reminder, setting: NotificationSetting) {
        val notificationDateTime = reminder.date
            .minusDays(setting.daysBeforeEvent.toLong())
            .atTime(setting.time)

        // Don't schedule notifications for past dates
        if (notificationDateTime.isBefore(LocalDateTime.now())) {
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_name", reminder.name)
            putExtra("reminder_class", reminder.className)
            putExtra("reminder_date", reminder.date.toString())
            putExtra("setting_id", setting.id)
            putExtra("setting_name", setting.name)
            putExtra("days_before", setting.daysBeforeEvent)
        }

        val requestCode = generateRequestCode(reminder.id, setting.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = notificationDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d("NotificationScheduler", "Scheduled notification for ${reminder.name} at $notificationDateTime")
        } catch (e: SecurityException) {
            Log.e("NotificationScheduler", "Failed to schedule notification: ${e.message}")
        }
    }

    fun cancelNotificationsForReminder(reminderId: String, notificationSettings: List<NotificationSetting>) {
        notificationSettings.forEach { setting ->
            cancelNotification(reminderId, setting.id)
        }
    }

    private fun cancelNotification(reminderId: String, settingId: String) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val requestCode = generateRequestCode(reminderId, settingId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun generateRequestCode(reminderId: String, settingId: String): Int {
        return ("$reminderId$settingId").hashCode()
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: ""
        val reminderName = intent.getStringExtra("reminder_name") ?: ""
        val reminderClass = intent.getStringExtra("reminder_class") ?: ""
        val reminderDateStr = intent.getStringExtra("reminder_date") ?: ""
        val settingName = intent.getStringExtra("setting_name") ?: ""
        val daysBeforeEvent = intent.getIntExtra("days_before", 0)

        if (reminderId.isNotEmpty() && reminderName.isNotEmpty()) {
            try {
                val reminderDate = java.time.LocalDate.parse(reminderDateStr)
                val reminder = Reminder(
                    id = reminderId,
                    name = reminderName,
                    className = reminderClass,
                    date = reminderDate
                )

                val notificationSetting = NotificationSetting(
                    id = intent.getStringExtra("setting_id") ?: "",
                    name = settingName,
                    daysBeforeEvent = daysBeforeEvent,
                    time = java.time.LocalTime.now(), // Time doesn't matter for notification display
                    isEnabled = true
                )

                val notificationHelper = NotificationHelper(context)
                val notificationId = generateNotificationId(reminderId, notificationSetting.id)
                notificationHelper.showReminderNotification(reminder, notificationSetting, notificationId)

            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error showing notification: ${e.message}")
            }
        }
    }

    private fun generateNotificationId(reminderId: String, settingId: String): Int {
        return ("$reminderId$settingId").hashCode()
    }
}
