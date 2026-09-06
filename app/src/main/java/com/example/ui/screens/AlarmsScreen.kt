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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.data.AlarmEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.WheelTimePicker
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FocusAlarmViewModel

@Composable
fun AlarmsScreen(
    viewModel: FocusAlarmViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.allAlarms.collectAsState()
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    if (editingAlarm != null || isCreatingNew) {
        val target = editingAlarm ?: AlarmEntity(
            hour = 6,
            minute = 30,
            label = "Morning Focus",
            repeatDays = "1,2,3,4,5",
            missionType = "MATH",
            missionCount = 3
        )
        AlarmEditSheet(
            alarm = target,
            isNew = isCreatingNew,
            onSave = { saved ->
                viewModel.saveAlarm(saved)
                editingAlarm = null
                isCreatingNew = false
            },
            onDelete = {
                if (editingAlarm != null) {
                    viewModel.deleteAlarm(editingAlarm!!)
                }
                editingAlarm = null
                isCreatingNew = false
            },
            onCancel = {
                editingAlarm = null
                isCreatingNew = false
            },
            onTestMission = {
                viewModel.triggerAlarmMissionTest(target)
            }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alarms",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            items(alarms.size) { index ->
                val alarm = alarms[index]
                AlarmItemCard(
                    alarm = alarm,
                    onToggle = { viewModel.toggleAlarm(alarm) },
                    onClick = { editingAlarm = alarm },
                    onTestMission = { viewModel.triggerAlarmMissionTest(alarm) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(90.dp)) // Clearance for FAB and Nav bar
            }
        }

        // Add Alarm FAB (matching screen-2-alarmlist.png)
        FloatingActionButton(
            onClick = { isCreatingNew = true },
            containerColor = AccentAmber,
            contentColor = DeepCharcoal,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Alarm",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onTestMission: () -> Unit
) {
    val h12 = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
    val amPm = if (alarm.hour >= 12) "PM" else "AM"
    val timeStr = String.format("%02d:%02d %s", h12, alarm.minute, amPm)
    val activeDays = AlarmScheduler.parseDays(alarm.repeatDays)

    val missionLabel = when (alarm.missionType) {
        "MATH" -> "Math problem"
        "QR" -> "Barcode scan"
        "SHAKE" -> "Shake"
        "MEMORY" -> "Memory puzzle"
        "PHRASE" -> "Inspirational phrase"
        else -> "Step challenge"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%02d:%02d", h12, alarm.minute),
                        color = if (alarm.isEnabled) TextWhite else TextDisabled,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = amPm,
                        color = if (alarm.isEnabled) TextMuted else TextDisabled,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepCharcoal,
                        checkedTrackColor = AccentAmber,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CharcoalElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alarm.label,
                color = if (alarm.isEnabled) TextMuted else TextDisabled,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Days row: M T W T F S S
                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                // In repeatDays: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 0=Sun
                val dayIndexes = listOf(1, 2, 3, 4, 5, 6, 0)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayLabels.forEachIndexed { i, label ->
                        val dayIdx = dayIndexes[i]
                        val isDayActive = activeDays.contains(dayIdx)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDayActive && alarm.isEnabled) AccentAmber.copy(alpha = 0.25f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isDayActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDayActive && alarm.isEnabled) AccentAmber else TextMuted.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Mission Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTestMission() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (alarm.isEnabled) AccentAmber else TextDisabled,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = missionLabel,
                        color = if (alarm.isEnabled) TextMuted else TextDisabled,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
