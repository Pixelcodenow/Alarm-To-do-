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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun TimelineSheet(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableStateOf(3) } // 3 = Thu 15

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Today's Timeline",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontal Day Selector (Mon 12 .. Sat 17)
        item {
            val days = listOf(
                "Mon" to "12",
                "Tue" to "13",
                "Wed" to "14",
                "Thu" to "15",
                "Fri" to "16",
                "Sat" to "17"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { idx, (dayName, dateNum) ->
                    val isSelected = idx == selectedDayIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) AccentViolet else GlassSurface)
                            .border(1.dp, if (isSelected) AccentViolet else GlassBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedDayIndex = idx }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                color = if (isSelected) DeepCharcoal else TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateNum,
                                fontSize = 16.sp,
                                color = if (isSelected) DeepCharcoal else TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Timeline Blocks (matches screen-7-planner-timeline.png)
        item {
            Text(
                text = "TIMELINE BLOCKS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // 05:33 AM: Wake Up Alarm
        item {
            TimelineBlockItem(
                time = "05:33 AM",
                title = "Wake Up Alarm",
                subtitle = "Math Mission • Ringing Success",
                accentColor = AccentAmber,
                icon = {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                }
            )
        }

        // 09:00 AM: Deep Work Session
        item {
            TimelineBlockItem(
                time = "09:00 AM",
                title = "Deep Work Session",
                subtitle = "Focus • 50 mins • Completed",
                accentColor = AccentViolet,
                icon = {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AccentViolet, modifier = Modifier.size(18.dp))
                }
            )
        }

        // 11:30 AM: Review Design Specs
        item {
            TimelineBlockItem(
                time = "11:30 AM",
                title = "Review Design Specs",
                subtitle = "Priority Task • In Progress",
                accentColor = AccentMint,
                icon = {
                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentMint, modifier = Modifier.size(18.dp))
                }
            )
        }

        // 02:00 PM: Power Nap Alarm
        item {
            TimelineBlockItem(
                time = "02:00 PM",
                title = "Power Nap Alarm",
                subtitle = "Smart Alarm • 20m Scheduled",
                accentColor = AccentAmber,
                icon = {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                }
            )
        }

        // 04:30 PM: Breathe & Reset
        item {
            TimelineBlockItem(
                time = "04:30 PM",
                title = "Breathe & Reset",
                subtitle = "Calm (4-4-6) • 5 mins Scheduled",
                accentColor = AccentMint,
                icon = {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = AccentMint, modifier = Modifier.size(18.dp))
                }
            )
        }

        // SUMMARY CARD (matches screen-7-planner-timeline.png)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DAY SUMMARY",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Planned Focus", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("4.5 hrs", color = AccentViolet, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Text("Tasks Linked", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("5 items", color = AccentMint, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Text("Alarms Set", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("3 active", color = AccentAmber, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TimelineBlockItem(
    time: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(68.dp)) {
            Text(time.substringBefore(" "), color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(time.substringAfter(" "), color = TextMuted, fontSize = 11.sp)
        }

        GlassCard(
            modifier = Modifier.weight(1f),
            borderColor = accentColor.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = title, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, color = accentColor, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
        }
    }
}
