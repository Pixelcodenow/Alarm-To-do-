package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AmbientSound
import com.example.data.TaskEntity
import com.example.ui.components.BreathOrb
import com.example.ui.components.CircularProgressTimer
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GlowGlassCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.BreathPattern
import com.example.ui.viewmodel.DefaultBreathPatterns
import com.example.ui.viewmodel.FocusAlarmViewModel
import com.example.ui.viewmodel.PomodoroMode

@Composable
fun FocusScreen(
    viewModel: FocusAlarmViewModel,
    initialTab: String = "POMODORO", // "POMODORO" or "BREATHE"
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(initialTab) }
    val isBreathActive by viewModel.isBreathActive.collectAsState()
    val isBreathCompleted by viewModel.isBreathCompleted.collectAsState()

    // If active breath session is underway, display fullscreen Active Breath View
    if (isBreathActive) {
        BreathSessionActiveView(viewModel = viewModel)
        return
    }

    if (isBreathCompleted) {
        BreathSessionCompleteView(viewModel = viewModel)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Segmented Switch: Pomodoro vs Breathe
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedSection == "POMODORO") AccentViolet else Color.Transparent)
                    .clickable { selectedSection = "POMODORO" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pomodoro",
                    color = if (selectedSection == "POMODORO") DeepCharcoal else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedSection == "BREATHE") AccentMint else Color.Transparent)
                    .clickable { selectedSection = "BREATHE" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Breathe",
                    color = if (selectedSection == "BREATHE") DeepCharcoal else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSection == "POMODORO") {
            PomodoroSectionView(viewModel = viewModel)
        } else {
            BreatheHomeView(viewModel = viewModel)
        }
    }
}

@Composable
fun PomodoroSectionView(viewModel: FocusAlarmViewModel) {
    val currentMode by viewModel.pomodoroMode.collectAsState()
    val totalSec by viewModel.pomodoroTotalSeconds.collectAsState()
    val remainingSec by viewModel.pomodoroRemainingSeconds.collectAsState()
    val isRunning by viewModel.isPomodoroRunning.collectAsState()
    val isPaused by viewModel.isPomodoroPaused.collectAsState()
    val attachedTask by viewModel.attachedTask.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val sessionCycle by viewModel.pomodoroSessionCycle.collectAsState()

    var showTaskPicker by remember { mutableStateOf(false) }

    // Format mm:ss
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)
    val progress = if (totalSec > 0) 1f - (remainingSec.toFloat() / totalSec.toFloat()) else 0f

    if (showTaskPicker) {
        AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "Attach Task to Session",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (activeTasks.isEmpty()) {
                        Text(text = "No active tasks found", color = TextMuted, fontSize = 14.sp)
                    } else {
                        activeTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (attachedTask?.id == task.id) GlassSurface else Color.Transparent)
                                    .clickable {
                                        viewModel.attachTaskToPomodoro(task)
                                        showTaskPicker = false
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.title,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTaskPicker = false }) {
                    Text("Close", color = AccentViolet)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Chips (matching screen-4-focustimer.png)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val modes = listOf(
                    PomodoroMode.DEEP_WORK to "Deep Work",
                    PomodoroMode.SHORT_BREAK to "Short Break",
                    PomodoroMode.LONG_BREAK to "Long Break"
                )
                modes.forEach { (m, label) ->
                    val isSelected = currentMode == m
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) AccentViolet else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) AccentViolet else GlassBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setPomodoroMode(m) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) DeepCharcoal else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // Circular Timer Display (matching screen-4-focustimer.png)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressTimer(
                progress = progress,
                timeText = timeText,
                label = currentMode.displayName,
                primaryColor = AccentViolet
            )
        }

        // Sessions Dots Indicator (matching screen-4-focustimer.png)
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..4) {
                        val isFilled = i <= sessionCycle
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) AccentViolet else CharcoalElevated)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$sessionCycle of 4 sessions",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        // Timer Controls: Reset, Big Play/Pause, Skip (matching screen-4-focustimer.png)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .clickable { viewModel.resetPomodoro() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Big Play / Pause Button (glowing violet)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(AccentViolet)
                        .clickable {
                            if (isRunning && !isPaused) viewModel.pausePomodoro()
                            else viewModel.startPomodoro()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning && !isPaused) "Pause" else "Play",
                        tint = DeepCharcoal,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Skip Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .clickable { viewModel.skipPomodoro() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // CURRENT TASK CARD (matching screen-4-focustimer.png)
        item {
            val task = attachedTask ?: activeTasks.firstOrNull()
            val taskTitle = task?.title ?: "FocusAlarm UX Flow Mapping"

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CURRENT TASK",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (task?.isCompleted == true) Icons.Default.Check else Icons.Outlined.Circle,
                                contentDescription = "Checkbox",
                                tint = if (task?.isCompleted == true) AccentMint else TextMuted,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        task?.let { viewModel.toggleTaskComplete(it) }
                                    }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = taskTitle,
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Change task",
                            tint = AccentViolet,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { showTaskPicker = true }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun BreatheHomeView(viewModel: FocusAlarmViewModel) {
    val selectedPattern by viewModel.selectedBreathPattern.collectAsState()
    var showCustomPatternSheet by remember { mutableStateOf(false) }

    if (showCustomPatternSheet) {
        CustomPatternSheet(
            onDismiss = { showCustomPatternSheet = false },
            onSave = { inhale, hold, exhale, postHold, duration ->
                viewModel.saveCustomPattern(inhale, hold, exhale, postHold, duration)
                showCustomPatternSheet = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Text (matches breath-home.png)
        item {
            Column {
                Text(
                    text = "Breathe",
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Slow down. Breathe in. Breathe out.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        }

        // Hero Card with glowing orb (matches breath-home.png)
        item {
            GlowGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = AccentMint.copy(alpha = 0.15f),
                onClick = { viewModel.startBreathSession() }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentMint.copy(alpha = 0.2f))
                            .border(2.dp, AccentMint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = AccentMint,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Ready to reset your mind?",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Take 5 mindful minutes to reduce cortisol and regain clarity.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassActionButton(
                        text = "Start Breathing",
                        primaryColor = AccentMint,
                        textColor = DeepCharcoal,
                        onClick = { viewModel.startBreathSession() }
                    )
                }
            }
        }

        // Recommended Section (matches breath-home.png)
        item {
            Text(
                text = "RECOMMENDED",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            // Calm Card
            val calm = DefaultBreathPatterns.first { it.id == "calm" }
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.selectBreathPattern(calm)
                    viewModel.startBreathSession()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = calm.name,
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = calm.description,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${calm.inhaleSeconds} — ${calm.holdSeconds} — ${calm.exhaleSeconds} • ${calm.defaultDurationMinutes} min",
                            color = AccentMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentMint)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Start",
                            color = DeepCharcoal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // All Patterns list (matches breath-home.png)
        item {
            Text(
                text = "ALL EXERCISES",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(DefaultBreathPatterns.size) { index ->
            val pattern = DefaultBreathPatterns[index]
            val isSelected = selectedPattern.id == pattern.id

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isSelected) AccentMint else GlassBorder,
                onClick = {
                    if (pattern.id == "custom") {
                        showCustomPatternSheet = true
                    } else {
                        viewModel.selectBreathPattern(pattern)
                        viewModel.startBreathSession()
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = pattern.name,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = pattern.description,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${pattern.inhaleSeconds}-${pattern.holdSeconds}-${pattern.exhaleSeconds}${if (pattern.postHoldSeconds > 0) "-${pattern.postHoldSeconds}" else ""} • ${pattern.defaultDurationMinutes} min",
                            color = AccentMint,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GlassSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = AccentMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun BreathSessionActiveView(viewModel: FocusAlarmViewModel) {
    val selectedPattern by viewModel.selectedBreathPattern.collectAsState()
    val isPaused by viewModel.isBreathPaused.collectAsState()
    val currentPhase by viewModel.currentBreathPhase.collectAsState()
    val secondsRemaining by viewModel.phaseSecondsRemaining.collectAsState()
    val phaseFraction by viewModel.phaseFraction.collectAsState()
    val totalRemainingSeconds by viewModel.breathTotalRemainingSeconds.collectAsState()
    val completedCycles by viewModel.breathCompletedCycles.collectAsState()
    val totalCycles by viewModel.breathTotalCycles.collectAsState()
    val ambientSound by viewModel.selectedAmbientSound.collectAsState()
    val ambientVolume by viewModel.ambientVolume.collectAsState()
    val chimeVolume by viewModel.chimeVolume.collectAsState()

    val mins = totalRemainingSeconds / 60
    val secs = totalRemainingSeconds % 60
    val timeRemainingStr = String.format("%d:%02d", mins, secs)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Pattern Badge & Close (X)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedPattern.name,
                    color = AccentMint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, CircleShape)
                    .clickable { viewModel.stopBreathSession() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Central Animated Breath Orb & Phase Indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BreathOrb(
                currentPhase = currentPhase,
                phaseSecondsRemaining = secondsRemaining,
                phaseFraction = phaseFraction,
                size = 260.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentPhase.displayName.uppercase(),
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$completedCycles of $totalCycles cycles • $timeRemainingStr remaining",
                color = TextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pause / Resume Action
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(AccentMint)
                    .clickable {
                        if (isPaused) viewModel.resumeBreathSession()
                        else viewModel.pauseBreathSession()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = DeepCharcoal,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Sound Controls Card (matches breath-session-active.png)
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOUNDSCAPE",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Preview",
                        color = AccentMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { viewModel.previewSound(ambientSound) }
                            .padding(2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Soundscape horizontal pills: Rain, Ocean, Forest, Soft Wind, Ambient, Fireplace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sounds = AmbientSound.values()
                    sounds.take(4).forEach { snd ->
                        val isSelected = snd == ambientSound
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentMint else GlassSurface)
                                .clickable { viewModel.setAmbientSound(snd) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = snd.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DeepCharcoal else TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sliders for Ambient and Chime Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ambient", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(55.dp))
                    Slider(
                        value = ambientVolume,
                        onValueChange = { viewModel.setAmbientVolume(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentMint,
                            activeTrackColor = AccentMint,
                            inactiveTrackColor = CharcoalElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chime", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(55.dp))
                    Slider(
                        value = chimeVolume,
                        onValueChange = { viewModel.setChimeVolume(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentViolet,
                            activeTrackColor = AccentViolet,
                            inactiveTrackColor = CharcoalElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BreathSessionCompleteView(viewModel: FocusAlarmViewModel) {
    val selectedPattern by viewModel.selectedBreathPattern.collectAsState()
    val durationMinutes by viewModel.breathSessionDurationMinutes.collectAsState()
    val totalCycles by viewModel.breathTotalCycles.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(AccentMint.copy(alpha = 0.2f))
                    .border(2.dp, AccentMint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = AccentMint,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Session Completed",
                color = TextWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Feel the calm within you. Take this peace into your next moments.",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3 Stat cards (matches breath-session-complete.png)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Duration", color = TextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$durationMinutes:00", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Completed", color = TextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalCycles cycles", color = AccentMint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Avg Breath", color = TextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${selectedPattern.totalCycleSeconds}s", color = AccentViolet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            GlassActionButton(
                text = "Breathe Again",
                primaryColor = AccentMint,
                textColor = DeepCharcoal,
                onClick = { viewModel.startBreathSession() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Done",
                color = TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { viewModel.stopBreathSession() }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun CustomPatternSheet(
    onDismiss: () -> Unit,
    onSave: (inhale: Int, hold: Int, exhale: Int, postHold: Int, durationMins: Int) -> Unit
) {
    var inhale by remember { mutableIntStateOf(4) }
    var hold by remember { mutableIntStateOf(2) }
    var exhale by remember { mutableIntStateOf(4) }
    var postHold by remember { mutableIntStateOf(2) }
    var duration by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepCharcoal,
        title = {
            Text(
                text = "Create Custom Breath Pattern",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Inhale row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Inhale (seconds): $inhale", color = TextWhite, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("-", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (inhale > 2) inhale-- }.padding(4.dp))
                        Text("+", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (inhale < 12) inhale++ }.padding(4.dp))
                    }
                }

                // Hold row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hold (seconds): $hold", color = TextWhite, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("-", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (hold > 0) hold-- }.padding(4.dp))
                        Text("+", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (hold < 12) hold++ }.padding(4.dp))
                    }
                }

                // Exhale row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exhale (seconds): $exhale", color = TextWhite, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("-", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (exhale > 2) exhale-- }.padding(4.dp))
                        Text("+", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (exhale < 12) exhale++ }.padding(4.dp))
                    }
                }

                // Post-Hold row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Post-Hold (seconds): $postHold", color = TextWhite, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("-", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (postHold > 0) postHold-- }.padding(4.dp))
                        Text("+", color = AccentMint, fontSize = 18.sp, modifier = Modifier.clickable { if (postHold < 12) postHold++ }.padding(4.dp))
                    }
                }

                // Duration row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration: $duration mins", color = AccentAmber, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("-", color = AccentAmber, fontSize = 18.sp, modifier = Modifier.clickable { if (duration > 1) duration-- }.padding(4.dp))
                        Text("+", color = AccentAmber, fontSize = 18.sp, modifier = Modifier.clickable { if (duration < 30) duration++ }.padding(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(inhale, hold, exhale, postHold, duration) }) {
                Text("Save & Use", color = AccentMint)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
