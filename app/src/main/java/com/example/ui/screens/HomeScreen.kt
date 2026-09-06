package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowGlassCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentAmberDark
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentIndigoButton
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FocusAlarmViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: FocusAlarmViewModel,
    onNavigateToFocus: () -> Unit,
    onNavigateToBreath: () -> Unit,
    onNavigateToAlarms: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onOpenTimeline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextAlarm by viewModel.nextAlarm.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    // Formatted current date: e.g. "Tuesday, May 23"
    val dateStr = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    // Next alarm display info
    val alarmHour12: String
    val alarmMinute: String
    val alarmAmPm: String
    if (nextAlarm != null) {
        val h = nextAlarm!!.hour
        val m = nextAlarm!!.minute
        val isPm = h >= 12
        val h12 = if (h % 12 == 0) 12 else h % 12
        alarmHour12 = String.format("%02d", h12)
        alarmMinute = String.format("%02d", m)
        alarmAmPm = if (isPm) "PM" else "AM"
    } else {
        alarmHour12 = "06"
        alarmMinute = "30"
        alarmAmPm = "AM"
    }

    val countdownStr = if (nextAlarm != null) {
        val remaining = AlarmScheduler.getTimeRemaining(nextAlarm!!.hour, nextAlarm!!.minute, nextAlarm!!.repeatDays)
        "Rings in ${remaining.first}h ${remaining.second}min"
    } else {
        "Rings in 7h 42min"
    }

    val streakDays = profile?.streakDays ?: 12

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HEADER (matches Professional Polish design: Date, Gradient Title, Streak Badge)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateStr,
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "FocusAlarm",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AccentViolet, AccentIndigo)
                            ),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Streak Badge: bg-white/5 backdrop-blur border-white/10 rounded-full
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(50))
                        .clickable { onOpenTimeline() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🔥", fontSize = 14.sp)
                        Text(
                            text = "$streakDays Day Streak",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. NEXT ALARM HERO CARD (rounded-[32px], ambient amber glow, big 5xl time, weekday pills)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
                    .clickable { onNavigateToAlarms() }
                    .padding(24.dp)
            ) {
                // Top-right ambient amber radial blur
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AccentAmberDark.copy(alpha = 0.22f), Color.Transparent)
                            )
                        )
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "NEXT ALARM",
                                color = AccentAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$alarmHour12:$alarmMinute",
                                    color = TextWhite,
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-1).sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = alarmAmPm,
                                    color = TextMuted,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = countdownStr,
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        // Circular alarm indicator: bg-amber-500/20 rounded-2xl
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AccentAmberDark.copy(alpha = 0.20f))
                                .clickable {
                                    nextAlarm?.let { viewModel.triggerAlarmMissionTest(it) }
                                        ?: onNavigateToAlarms()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alarm status",
                                tint = AccentAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Weekday pills: M T W T F S S
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val scheduledDays = AlarmScheduler.parseDays(nextAlarm?.repeatDays ?: "1,2,3,4,5")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        days.forEachIndexed { index, dayLetter ->
                            val isScheduled = scheduledDays.contains(index + 1)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isScheduled) TextWhite else GlassSurface)
                                    .border(
                                        1.dp,
                                        if (isScheduled) Color.Transparent else GlassBorderSubtle,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLetter,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isScheduled) DeepCharcoal else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 2-COLUMN GRID: FOCUS CARD & BREATH CARD (rounded-3xl, h-32 flex justify-between)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Focus Card (Left)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                        .clickable { onNavigateToFocus() }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentViolet.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = "Focus",
                                    tint = AccentViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "FOCUS",
                                color = AccentViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Column {
                            Text(
                                text = "25:00",
                                color = TextWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Pomodoro Session",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Breath Card (Right)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                        .clickable { onNavigateToBreath() }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentCyan.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = "Breath",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "BREATH",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Column {
                            Text(
                                text = "Box Breath",
                                color = TextWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "4-4-4-4 Rhythm",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. PRIORITY TASKS SECTION (rounded-3xl, custom rounded checkboxes, subtitle tags)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority Tasks",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // "3 Active" badge in bg-indigo-500/20 text-indigo-400
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AccentIndigo.copy(alpha = 0.20f))
                                .clickable { onNavigateToTasks() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${activeTasks.size} Active",
                                color = AccentIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Display active tasks
                        val displayTasks = activeTasks.take(3)
                        if (displayTasks.isEmpty()) {
                            Text(
                                text = "No pending priority tasks",
                                color = TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            displayTasks.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(GlassSurface)
                                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom rounded-md square checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (task.isCompleted) AccentIndigoButton else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (task.isCompleted) AccentIndigoButton else Color(0x33FFFFFF),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { viewModel.toggleTaskComplete(task) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (task.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed",
                                                tint = TextWhite,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Due ${task.dueDate.lowercase()} • ${task.tag}",
                                            color = TextDisabled,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Completed sample item if completed tasks exist
                        val firstCompleted = completedTasks.firstOrNull()
                        if (firstCompleted != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GlassSurface.copy(alpha = 0.5f))
                                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentIndigoButton)
                                        .clickable { viewModel.toggleTaskComplete(firstCompleted) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = TextWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = firstCompleted.title,
                                    color = TextDisabled,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. TODAY'S TIMELINE LINK
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { onOpenTimeline() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = AccentViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "View Today's Timeline Schedule",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Explore →",
                        color = AccentViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
