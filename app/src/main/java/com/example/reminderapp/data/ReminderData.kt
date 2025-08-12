package com.example.reminderapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey
    val id: String,
    val name: String,
    val className: String,
    val date: LocalDate,
    val isTrackable: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class SchoolClass(
    val name: String,
    val color: String // Hex color for visual distinction
)

object SchoolClasses {
    val defaultClasses = listOf(
        SchoolClass("Physics", "#FF6B6B"),
        SchoolClass("Web App Dev", "#4ECDC4"),
        SchoolClass("Mobile App Dev", "#45B7D1"),
        SchoolClass("Computer Vision", "#FFA07A"),
        SchoolClass("Artificial Intelligence", "#98D8C8"),
        SchoolClass("US History", "#F7DC6F"),
        SchoolClass("Calculus", "#BB8FCE"),
        SchoolClass("Extracurricular", "#85C1E9")
    )
}
