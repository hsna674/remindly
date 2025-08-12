package com.example.reminderapp.data

import java.time.LocalTime

data class NotificationSetting(
    val id: String,
    val name: String,
    val daysBeforeEvent: Int,
    val time: LocalTime,
    val isEnabled: Boolean = true
)

object DefaultNotificationSettings {
    val defaultSettings = listOf(
        NotificationSetting(
            id = "two_days_before",
            name = "2 Days Before",
            daysBeforeEvent = 2,
            time = LocalTime.of(17, 30), // 5:30 PM
            isEnabled = true
        ),
        NotificationSetting(
            id = "one_day_before",
            name = "1 Day Before",
            daysBeforeEvent = 1,
            time = LocalTime.of(17, 30), // 5:30 PM
            isEnabled = true
        ),
        NotificationSetting(
            id = "day_of",
            name = "Day Of",
            daysBeforeEvent = 0,
            time = LocalTime.of(7, 30), // 7:30 AM
            isEnabled = true
        )
    )
}
