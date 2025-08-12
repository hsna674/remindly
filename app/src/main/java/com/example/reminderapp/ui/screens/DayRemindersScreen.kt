package com.example.reminderapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.data.SchoolClass
import com.example.reminderapp.data.SchoolClasses
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DayRemindersScreen(
    selectedDate: LocalDate,
    reminders: List<Reminder>,
    availableClasses: List<SchoolClass> = SchoolClasses.defaultClasses,
    onNavigateBack: () -> Unit,
    onEditReminder: (Reminder) -> Unit = {},
    onDeleteReminder: (Reminder) -> Unit = {},
    onToggleComplete: (Reminder, Boolean) -> Unit = { _, _ -> }
) {
    // Group reminders by class
    val remindersByClass = reminders.groupBy { it.className }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Reminders",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Content
        if (reminders.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Reminders",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You don't have any reminders for this date.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Reminders list with staggered animations
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                remindersByClass.entries.forEachIndexed { index, (className, classReminders) ->
                    item(key = className) {
                        // Staggered animation for each class section
                        var isVisible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            delay(index * 100L) // Stagger by 100ms
                            isVisible = true
                        }

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(durationMillis = 400, easing = EaseOutCubic)
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 400, easing = EaseOutCubic)
                            )
                        ) {
                            ClassReminderSection(
                                className = className,
                                reminders = classReminders,
                                availableClasses = availableClasses,
                                onEditReminder = onEditReminder,
                                onDeleteReminder = onDeleteReminder,
                                onToggleComplete = onToggleComplete
                            )
                        }
                    }
                }

                // Add some bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ClassReminderSection(
    className: String,
    reminders: List<Reminder>,
    availableClasses: List<SchoolClass> = SchoolClasses.defaultClasses,
    onEditReminder: (Reminder) -> Unit = {},
    onDeleteReminder: (Reminder) -> Unit = {},
    onToggleComplete: (Reminder, Boolean) -> Unit = { _, _ -> }
) {
    val classInfo = availableClasses.find { it.name == className }
    val classColor = classInfo?.color ?: "#95A5A6"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Class header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(classColor.removePrefix("#").toLong(16) or 0xFF000000))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = className,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${reminders.size} reminder${if (reminders.size > 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminders for this class
            reminders.forEach { reminder ->
                ReminderItem(
                    reminder = reminder,
                    classColor = classColor,
                    onEditReminder = onEditReminder,
                    onDeleteReminder = onDeleteReminder,
                    onToggleComplete = onToggleComplete
                )

                if (reminder != reminders.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReminderItem(
    reminder: Reminder,
    classColor: String,
    onEditReminder: (Reminder) -> Unit = {},
    onDeleteReminder: (Reminder) -> Unit = {},
    onToggleComplete: (Reminder, Boolean) -> Unit = { _, _ -> }
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    val textColor = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    val alpha = if (reminder.isCompleted) 0.6f else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (reminder.isTrackable) {
                                onToggleComplete(reminder, !reminder.isCompleted)
                            }
                        },
                        onLongClick = { showOptionsMenu = true }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Always show class color dot for consistency
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(classColor.removePrefix("#").toLong(16) or 0xFF000000))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title
                Text(
                    text = reminder.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = alpha),
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Compact completion toggle at the end (only if trackable)
                if (reminder.isTrackable) {
                    Spacer(modifier = Modifier.width(12.dp))
                    CompletionToggle(
                        checked = reminder.isCompleted,
                        onCheckedChange = { checked -> onToggleComplete(reminder, checked) }
                    )
                }
            }

            // Options dropdown menu
            DropdownMenu(
                expanded = showOptionsMenu,
                onDismissRequest = { showOptionsMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Edit",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        showOptionsMenu = false
                        onEditReminder(reminder)
                    }
                )

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Delete",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    onClick = {
                        showOptionsMenu = false
                        onDeleteReminder(reminder)
                    }
                )
            }
        }
    }
}

@Composable
private fun CompletionToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val bgColor by animateColorAsState(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, label = "bg")
    val contentColor by animateColorAsState(if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "fg")
    val borderColor by animateColorAsState(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), label = "bd")

    Surface(
        color = bgColor,
        contentColor = contentColor,
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .clickable { onCheckedChange(!checked) },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    modifier = Modifier.size(14.dp)
                )
            } else {
                // Empty center; circle indicates tap target
                Spacer(modifier = Modifier.size(14.dp))
            }
        }
    }
}
