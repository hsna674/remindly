package com.example.reminderapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Animation imports
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.example.reminderapp.ui.theme.ReminderAppTheme
import com.example.reminderapp.ui.screens.AddReminderScreen
import com.example.reminderapp.ui.screens.DayRemindersScreen
import com.example.reminderapp.ui.screens.EditReminderScreen
import com.example.reminderapp.ui.screens.SettingsScreen
import com.example.reminderapp.ui.screens.UpcomingTasksScreen
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.data.SchoolClass
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.toColorInt
import com.example.reminderapp.viewmodel.ReminderViewModel

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // No-op for now
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val viewModel: ReminderViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            ReminderAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReminderApp()
                }
            }
        }
    }
}

@Composable
fun ReminderApp() {
    val viewModel: ReminderViewModel = viewModel()

    var currentScreen by remember { mutableStateOf("calendar") }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }

    // Collect state from ViewModel
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val selectedDateReminders by viewModel.selectedDateReminders.collectAsState()
    val schoolClasses by viewModel.schoolClasses.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()

    // Animated content transition
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            // Define your transition animations here
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn() togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }
            ) + fadeOut()
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            "calendar" -> CalendarScreen(
                selectedDate = selectedDate,
                reminders = selectedDateReminders,
                availableClasses = schoolClasses,
                onDateSelected = { viewModel.selectDate(it) },
                onAddReminderClick = { currentScreen = "add_reminder" },
                onSeeMoreClick = { currentScreen = "day_reminders" },
                onSettingsClick = { currentScreen = "settings" },
                onUpcomingTasksClick = { currentScreen = "upcoming_tasks" }
            )
            "add_reminder" -> AddReminderScreen(
                selectedDate = selectedDate,
                availableClasses = schoolClasses,
                onNavigateBack = { currentScreen = "calendar" },
                onReminderSaved = { reminder ->
                    viewModel.addReminder(reminder)
                    currentScreen = "calendar"
                }
            )
            "day_reminders" -> DayRemindersScreen(
                selectedDate = selectedDate,
                reminders = selectedDateReminders,
                availableClasses = schoolClasses,
                onNavigateBack = { currentScreen = "calendar" },
                onEditReminder = { reminder ->
                    reminderToEdit = reminder
                    currentScreen = "edit_reminder"
                },
                onDeleteReminder = { reminder ->
                    viewModel.deleteReminder(reminder)
                },
                onToggleComplete = { reminder, completed ->
                    viewModel.setReminderCompleted(reminder, completed)
                }
            )
            "edit_reminder" -> {
                reminderToEdit?.let { reminder ->
                    EditReminderScreen(
                        reminder = reminder,
                        availableClasses = schoolClasses,
                        onNavigateBack = {
                            reminderToEdit = null
                            currentScreen = "day_reminders"
                        },
                        onReminderUpdated = { updatedReminder ->
                            viewModel.updateReminder(updatedReminder)
                            reminderToEdit = null
                            currentScreen = "day_reminders"
                        },
                        onReminderDeleted = { reminderToDelete ->
                            viewModel.deleteReminder(reminderToDelete)
                            reminderToEdit = null
                            currentScreen = "day_reminders"
                        }
                    )
                }
            }
            "settings" -> SettingsScreen(
                classes = schoolClasses,
                notificationSettings = notificationSettings,
                isDarkMode = viewModel.isDarkMode.collectAsState().value,
                onNavigateBack = { currentScreen = "calendar" },
                onClassesUpdated = { newClasses ->
                    viewModel.updateSchoolClasses(newClasses)
                },
                onNotificationSettingsUpdated = { newSettings ->
                    viewModel.updateNotificationSettings(newSettings)
                },
                onDarkModeToggled = { enabled ->
                    viewModel.toggleDarkMode(enabled)
                }
            )
            "upcoming_tasks" -> UpcomingTasksScreen(
                reminders = allReminders,
                availableClasses = schoolClasses,
                onNavigateBack = { currentScreen = "calendar" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    selectedDate: LocalDate,
    reminders: List<Reminder>,
    availableClasses: List<SchoolClass> = emptyList(),
    onDateSelected: (LocalDate) -> Unit,
    onAddReminderClick: () -> Unit,
    onSeeMoreClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUpcomingTasksClick: () -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val daysOfWeek = remember { daysOfWeek() }

    // Collect all reminders from the parent component
    val viewModel: ReminderViewModel = viewModel()
    val allReminders by viewModel.allReminders.collectAsState()

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    // Track the currently visible month for display
    val visibleMonth by remember {
        derivedStateOf { state.firstVisibleMonth.yearMonth }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reminder),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(32.dp).padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Remindly",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Month/Year Header
            Text(
                text = visibleMonth.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy")
                ),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Days of Week Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    CalendarDay(
                        day = day,
                        isSelected = selectedDate == day.date,
                        isToday = today == day.date,
                        hasReminders = allReminders.any { it.date == day.date },
                        onClick = { onDateSelected(day.date) }
                    )
                },
                monthHeader = { /* Empty since we have custom header */ },
                monthBody = { _, content ->
                    Column {
                        content()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selected Date Info with Reminder Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Selected Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reminders preview
                    val dayReminders = reminders.filter { it.date == selectedDate }

                    if (dayReminders.isEmpty()) {
                        Text(
                            text = "No reminders scheduled",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        // Show preview of first 2 reminders
                        val previewReminders = dayReminders.take(2)

                        previewReminders.forEach { reminder ->
                            val classInfo = availableClasses.find { it.name == reminder.className }
                            val classColor = classInfo?.color ?: "#95A5A6"

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(classColor.toColorInt()))
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = reminder.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = reminder.className,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Show "and X more" if there are additional reminders
                        if (dayReminders.size > 2) {
                            Text(
                                text = "and ${dayReminders.size - 2} more...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // See all button if there are reminders
                        if (dayReminders.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = onSeeMoreClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "View All Reminders (${dayReminders.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Navigation with rounded card background
            Box (
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(min = 120.dp, max = 250.dp)
                        .padding(horizontal = 50.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Add Reminder
                        Column(
                            modifier = Modifier.clickable { onAddReminderClick() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Reminder",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Upcoming Tasks
                        Column(
                            modifier = Modifier.clickable { onUpcomingTasksClick() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Upcoming Tasks",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Settings
                        Column(
                            modifier = Modifier.clickable { onSettingsClick() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    hasReminders: Boolean,
    onClick: () -> Unit
) {
    // Animation states
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
            day.position != DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic)
    )

    // Animated reminder dot
    val dotScale by animateFloatAsState(
        targetValue = if (hasReminders) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (day.position == DayPosition.MonthDate) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (day.position == DayPosition.MonthDate) {
                            onClick()
                        }
                    }
                )
            }
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Date number - always centered
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 16.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )

        // Animated dot indicator for reminders
        if (hasReminders) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer {
                        scaleX = dotScale
                        scaleY = dotScale
                    }
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
            )
        }
    }
}
