package com.example.reminderapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.reminderapp.MainActivity
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.data.NotificationSetting
import java.time.format.DateTimeFormatter

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "reminder_notifications_v2" // Changed ID to force recreation
        const val CHANNEL_NAME = "Reminder Notifications"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming school reminders"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete old channel first (if it exists)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                notificationManager.deleteNotificationChannel("reminder_notifications")
            } catch (_: Exception) {
                // Ignore if channel doesn't exist
            }

            // Create new channel with sound
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                enableLights(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showReminderNotification(
        reminder: Reminder,
        notificationSetting: NotificationSetting,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (notificationSetting.daysBeforeEvent) {
            0 -> "Today: ${reminder.name}"
            1 -> "Tomorrow: ${reminder.name}"
            else -> "In ${notificationSetting.daysBeforeEvent} days: ${reminder.name}"
        }

        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
        val formattedDate = reminder.date.format(dateFormatter)

        val content = "${reminder.className} • $formattedDate"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setLights(android.graphics.Color.BLUE, 3000, 3000)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
