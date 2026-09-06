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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.audio.ProceduralAudioEngine
import com.example.data.AlarmEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.WheelTimePicker
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun AlarmEditSheet(
    alarm: AlarmEntity,
    isNew: Boolean,
    onSave: (AlarmEntity) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onTestMission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hour by remember { mutableStateOf(alarm.hour) }
    var minute by remember { mutableStateOf(alarm.minute) }
    var label by remember { mutableStateOf(alarm.label) }
    var repeatDaysSet by remember {
        mutableStateOf(AlarmScheduler.parseDays(alarm.repeatDays).toMutableSet())
    }
    var missionType by remember { mutableStateOf(alarm.missionType) }
    var missionDifficulty by remember { mutableStateOf(alarm.missionDifficulty) }
    var missionCount by remember { mutableStateOf(alarm.missionCount) }
    var soundName by remember { mutableStateOf(alarm.soundName) }
    var isSnoozeEnabled by remember { mutableStateOf(alarm.isSnoozeEnabled) }
    var isSmartWake by remember { mutableStateOf(alarm.isSmartWake) }

    var showMissionPicker by remember { mutableStateOf(false) }
    var showSoundPicker by remember { mutableStateOf(false) }
    var isPreviewingSound by remember { mutableStateOf(false) }

    val audioEngine = remember { ProceduralAudioEngine() }

    val repeatDaysString = repeatDaysSet.joinToString(",")

    // Mission Picker Dialog
    if (showMissionPicker) {
        AlertDialog(
            onDismissRequest = { showMissionPicker = false },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "Select Wake-Up Mission",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val missions = listOf(
                        "MATH" to "Math Equations (3 Problems • Easy)",
                        "SHAKE" to "Shake Device (25 Shakes)",
                        "MEMORY" to "Memory Match (Sequences)",
                        "PHRASE" to "Inspirational Phrase Typing",
                        "QR" to "QR / Barcode Verification",
                        "MOVEMENT" to "Movement Steps Challenge"
                    )
                    missions.forEach { (type, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (missionType == type) GlassSurface else Color.Transparent)
                                .clickable {
                                    missionType = type
                                    showMissionPicker = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (missionType == type),
                                onClick = {
                                    missionType = type
                                    showMissionPicker = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = desc, color = TextWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMissionPicker = false }) {
                    Text("Close", color = AccentAmber)
                }
            }
        )
    }

    // Sound Picker Dialog
    if (showSoundPicker) {
        AlertDialog(
            onDismissRequest = {
                showSoundPicker = false
                audioEngine.stop()
                isPreviewingSound = false
            },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "Select Alarm Sound",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val sounds = listOf(
                    "Zen Forest (Progressive)",
                    "Sunrise Radiance",
                    "Ocean Dawn",
                    "Gentle Flow",
                    "Cosmic Pulse"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sounds.forEach { sName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (soundName == sName) GlassSurface else Color.Transparent)
                                .clickable {
                                    soundName = sName
                                    audioEngine.playBellChime(587.33)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (soundName == sName),
                                onClick = { soundName = sName },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = sName, color = TextWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSoundPicker = false
                    audioEngine.stop()
                    isPreviewingSound = false
                }) {
                    Text("Select", color = AccentAmber)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action Bar: Cancel, Title, Save
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancel",
                    color = TextMuted,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable {
                            audioEngine.stop()
                            onCancel()
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )

                Text(
                    text = if (isNew) "New Alarm" else "Edit Alarm",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Save",
                    color = AccentAmber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            audioEngine.stop()
                            onSave(
                                alarm.copy(
                                    hour = hour,
                                    minute = minute,
                                    label = label.ifBlank { "Alarm" },
                                    repeatDays = repeatDaysString,
                                    missionType = missionType,
                                    missionDifficulty = missionDifficulty,
                                    missionCount = missionCount,
                                    soundName = soundName,
                                    isSnoozeEnabled = isSnoozeEnabled,
                                    isSmartWake = isSmartWake,
                                    isEnabled = true
                                )
                            )
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
        }

        // Modern Scrollable Wheel Time Picker
        item {
            WheelTimePicker(
                initialHour = hour,
                initialMinute = minute,
                repeatDays = repeatDaysString,
                onTimeChanged = { h24, m ->
                    hour = h24
                    minute = m
                }
            )
        }

        // Label Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Label",
                        color = TextMuted,
                        fontSize = 15.sp
                    )

                    BasicTextField(
                        value = label,
                        onValueChange = { label = it },
                        textStyle = TextStyle(
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(AccentAmber),
                        singleLine = true,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        // REPEAT WEEKDAYS (matches screen-3-alarmedit.png)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "REPEAT",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val days = listOf(
                        "Mon" to 1,
                        "Tue" to 2,
                        "Wed" to 3,
                        "Thu" to 4,
                        "Fri" to 5,
                        "Sat" to 6,
                        "Sun" to 0
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { (name, dayNum) ->
                            val isSelected = repeatDaysSet.contains(dayNum)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AccentAmber else GlassSurface)
                                    .clickable {
                                        val newSet = repeatDaysSet.toMutableSet()
                                        if (isSelected) newSet.remove(dayNum) else newSet.add(dayNum)
                                        repeatDaysSet = newSet
                                    }
                                    .padding(horizontal = 9.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) DeepCharcoal else TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // WAKE-UP MISSION SECTION
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WAKE-UP MISSION",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Test Mission",
                            color = AccentAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { onTestMission() }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selected Mission Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CharcoalElevated)
                            .border(1.5.dp, AccentAmber.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .clickable { showMissionPicker = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = when (missionType) {
                                        "MATH" -> "Math Equations"
                                        "QR" -> "Barcode / QR Scan"
                                        "SHAKE" -> "Shake Challenge"
                                        "MEMORY" -> "Memory Puzzle"
                                        "PHRASE" -> "Phrase Typing"
                                        else -> "Movement Mission"
                                    },
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = when (missionType) {
                                    "MATH" -> "$missionCount Problems • $missionDifficulty"
                                    "SHAKE" -> "20 Shakes"
                                    else -> "Interactive"
                                },
                                color = AccentAmber,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // ALARM SOUND & SNOOZE SECTION
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Alarm sound
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showSoundPicker = true }
                        ) {
                            Text(
                                text = "Alarm Sound",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = soundName,
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        // Preview button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GlassSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isPreviewingSound) {
                                        audioEngine.stop()
                                        isPreviewingSound = false
                                    } else {
                                        isPreviewingSound = true
                                        audioEngine.playBellChime(528.0)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isPreviewingSound) "Stop" else "Preview",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Snooze row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Snooze",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "3 minutes, max 3 times",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Switch(
                            checked = isSnoozeEnabled,
                            onCheckedChange = { isSnoozeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepCharcoal,
                                checkedTrackColor = AccentAmber,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CharcoalElevated
                            )
                        )
                    }
                }
            }
        }

        // Delete Button (if editing existing)
        if (!isNew) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Delete Alarm",
                        color = AccentCoral,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
