package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ProceduralAudioEngine
import com.example.data.AlarmEntity
import com.example.ui.components.GlassActionButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlin.math.sqrt

@Composable
fun WakeUpMissionScreen(
    alarm: AlarmEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioEngine = remember { ProceduralAudioEngine() }

    var currentProblemIndex by remember { mutableIntStateOf(1) }
    val totalProblems = if (alarm.missionType == "MATH") alarm.missionCount.coerceAtLeast(1) else 1
    var isCompleted by remember { mutableStateOf(false) }

    // Math problem generation
    val (mathQuestion, expectedAnswer) = remember(currentProblemIndex) {
        when (currentProblemIndex) {
            1 -> Pair("47 + 38", 85)
            2 -> Pair("14 × 6", 84)
            3 -> Pair("92 - 37", 55)
            else -> Pair("25 + 49", 74)
        }
    }
    var mathInput by remember(currentProblemIndex) { mutableStateOf("") }
    var mathError by remember(currentProblemIndex) { mutableStateOf(false) }

    // Shake challenge state
    var shakeCount by remember { mutableIntStateOf(0) }
    val targetShakes = 20

    // Accelerometer listener for Shake
    DisposableEffect(alarm.missionType) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastAccel = 0f
        var currentAccel = SensorManager.GRAVITY_EARTH
        var shakeThreshold = 14f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || alarm.missionType != "SHAKE" || isCompleted) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                lastAccel = currentAccel
                currentAccel = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = currentAccel - lastAccel
                if (delta > shakeThreshold) {
                    shakeCount++
                    if (shakeCount >= targetShakes) {
                        isCompleted = true
                        audioEngine.playBellChime(528.0)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (alarm.missionType == "SHAKE" && accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
            audioEngine.stop()
        }
    }

    // Phrase challenge state
    val targetPhrase = "I wake up energized, focused and ready"
    var enteredPhrase by remember { mutableStateOf("") }

    if (isCompleted) {
        // Celebratory Completion Screen
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DeepCharcoal)
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(AccentMint.copy(alpha = 0.2f))
                        .border(2.dp, AccentMint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = AccentMint,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Mission Complete!",
                    color = TextWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Good morning! You're awake and ready to conquer your day.",
                    color = TextMuted,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                GlassActionButton(
                    text = "Turn Off Alarm",
                    primaryColor = AccentMint,
                    textColor = DeepCharcoal,
                    onClick = onDismiss
                )
            }
        }
        return
    }

    // Active Mission Screen
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            val h12 = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
            val amPm = if (alarm.hour >= 12) "PM" else "AM"
            Text(
                text = String.format("%02d:%02d %s", h12, alarm.minute, amPm),
                color = AccentAmber,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "WAKE UP MISSION",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Solve challenge to dismiss alarm",
                color = TextMuted,
                fontSize = 13.sp
            )
        }

        // Challenge Body based on type
        when (alarm.missionType) {
            "MATH" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PROBLEM $currentProblemIndex OF $totalProblems",
                        color = AccentViolet,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "$mathQuestion = ?",
                        color = TextWhite,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Answer display field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CharcoalCard)
                            .border(
                                1.5.dp,
                                if (mathError) AccentCoral else GlassBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (mathInput.isEmpty()) "Enter answer" else mathInput,
                            color = if (mathInput.isEmpty()) TextMuted else TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Keypad: 1-9, C, 0, OK
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "OK")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        keys.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp, 56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (key == "OK") AccentAmber
                                                else if (key == "C") CharcoalElevated
                                                else GlassSurface
                                            )
                                            .clickable {
                                                when (key) {
                                                    "C" -> {
                                                        if (mathInput.isNotEmpty()) {
                                                            mathInput = mathInput.dropLast(1)
                                                        }
                                                        mathError = false
                                                    }
                                                    "OK" -> {
                                                        val userAns = mathInput.toIntOrNull()
                                                        if (userAns == expectedAnswer) {
                                                            audioEngine.playBellChime(528.0)
                                                            if (currentProblemIndex >= totalProblems) {
                                                                isCompleted = true
                                                            } else {
                                                                currentProblemIndex++
                                                            }
                                                        } else {
                                                            mathError = true
                                                        }
                                                    }
                                                    else -> {
                                                        if (mathInput.length < 5) {
                                                            mathInput += key
                                                        }
                                                        mathError = false
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (key == "OK") DeepCharcoal else TextWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "SHAKE" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(AccentAmber.copy(alpha = 0.15f))
                            .border(2.dp, AccentAmber, CircleShape)
                            .clickable {
                                shakeCount++
                                if (shakeCount >= targetShakes) {
                                    isCompleted = true
                                    audioEngine.playBellChime(528.0)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Shake",
                            tint = AccentAmber,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Shake Phone Vigorously",
                        color = TextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$shakeCount / $targetShakes Shakes",
                        color = AccentAmber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Or tap the circle above to simulate shakes",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            "PHRASE" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Type the phrase accurately:",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"$targetPhrase\"",
                        color = AccentAmber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    BasicTextField(
                        value = enteredPhrase,
                        onValueChange = {
                            enteredPhrase = it
                            if (it.trim().equals(targetPhrase.trim(), ignoreCase = true)) {
                                isCompleted = true
                                audioEngine.playBellChime(528.0)
                            }
                        },
                        textStyle = TextStyle(color = TextWhite, fontSize = 16.sp),
                        cursorBrush = SolidColor(AccentAmber),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CharcoalElevated)
                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    )
                }
            }

            else -> {
                // QR / Barcode / Movement fallback challenge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Scan Registered Barcode",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan bathroom toothpaste or coffee barcode",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GlassActionButton(
                        text = "Verify Barcode (Success)",
                        primaryColor = AccentAmber,
                        textColor = DeepCharcoal,
                        onClick = {
                            isCompleted = true
                            audioEngine.playBellChime(528.0)
                        }
                    )
                }
            }
        }

        // Bottom Emergency Actions: Snooze 3m & Emergency dismiss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Snooze (3m)",
                color = AccentAmber,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(8.dp)
            )

            Text(
                text = "Emergency Stop",
                color = AccentCoral,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(8.dp)
            )
        }
    }
}
