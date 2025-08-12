package com.example.reminderapp.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()

    fun getRemindersByDate(date: LocalDate): Flow<List<Reminder>> =
        reminderDao.getRemindersByDate(date)

    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteAllReminders() {
        reminderDao.deleteAllReminders()
    }
}
