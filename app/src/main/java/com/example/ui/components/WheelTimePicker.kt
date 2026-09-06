package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(
    initialHour: Int, // 0..23
    initialMinute: Int, // 0..59
    repeatDays: String,
    onTimeChanged: (hour24: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 12-hour format representation
    var isPm by remember(initialHour) { mutableStateOf(initialHour >= 12) }
    var selectedHour12 by remember(initialHour) {
        val h = if (initialHour % 12 == 0) 12 else initialHour % 12
        mutableStateOf(h)
    }
    var selectedMinute by remember(initialMinute) { mutableStateOf(initialMinute) }

    // Direct input mode toggle
    var isDirectInputMode by remember { mutableStateOf(false) }
    var directHourText by remember(selectedHour12) { mutableStateOf(String.format("%02d", selectedHour12)) }
    var directMinuteText by remember(selectedMinute) { mutableStateOf(String.format("%02d", selectedMinute)) }

    fun notifyChanged(h12: Int, min: Int, pm: Boolean) {
        val h24 = when {
            pm && h12 == 12 -> 12
            pm -> h12 + 12
            !pm && h12 == 12 -> 0
            else -> h12
        }
        onTimeChanged(h24, min)
    }

    // Calculate live "Ring in XX hr XX min"
    val currentHour24 = when {
        isPm && selectedHour12 == 12 -> 12
        isPm -> selectedHour12 + 12
        !isPm && selectedHour12 == 12 -> 0
        else -> selectedHour12
    }
    val timeRemaining = remember(currentHour24, selectedMinute, repeatDays) {
        AlarmScheduler.getTimeRemaining(currentHour24, selectedMinute, repeatDays)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Live "Ring in" Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Ring in ${timeRemaining.first} hr ${timeRemaining.second} min",
                color = AccentAmber,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isDirectInputMode) {
            // Direct Numeric Input Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                BasicTextField(
                    value = directHourText,
                    onValueChange = { input ->
                        if (input.length <= 2 && input.all { it.isDigit() }) {
                            directHourText = input
                            val num = input.toIntOrNull()
                            if (num != null && num in 1..12) {
                                selectedHour12 = num
                                notifyChanged(num, selectedMinute, isPm)
                            }
                        }
                    },
                    textStyle = TextStyle(
                        color = TextWhite,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(AccentViolet),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .width(90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp)
                )

                Text(
                    text = ":",
                    color = TextWhite,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                BasicTextField(
                    value = directMinuteText,
                    onValueChange = { input ->
                        if (input.length <= 2 && input.all { it.isDigit() }) {
                            directMinuteText = input
                            val num = input.toIntOrNull()
                            if (num != null && num in 0..59) {
                                selectedMinute = num
                                notifyChanged(selectedHour12, num, isPm)
                            }
                        }
                    },
                    textStyle = TextStyle(
                        color = TextWhite,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(AccentViolet),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { isDirectInputMode = false }
                    ),
                    modifier = Modifier
                        .width(90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // AM / PM Column
                Column {
                    Text(
                        text = "AM",
                        color = if (!isPm) AccentAmber else TextMuted,
                        fontSize = 18.sp,
                        fontWeight = if (!isPm) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isPm = false
                                notifyChanged(selectedHour12, selectedMinute, false)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PM",
                        color = if (isPm) AccentAmber else TextMuted,
                        fontSize = 18.sp,
                        fontWeight = if (isPm) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isPm = true
                                notifyChanged(selectedHour12, selectedMinute, true)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Tap to switch to swipe wheel",
                color = AccentViolet,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { isDirectInputMode = false }
                    .padding(8.dp)
            )
        } else {
            // Modern Scrollable Wheel Picker
            val itemHeight = 60.dp
            val visibleCount = 3
            val height = itemHeight * visibleCount

            val hourItems = (1..12).toList()
            val minuteItems = (0..59).toList()

            // Large virtual multiplier for infinite feeling wheel
            val multiplier = 100
            val hourInitialIndex = (multiplier / 2) * 12 + (selectedHour12 - 1)
            val minuteInitialIndex = (multiplier / 2) * 60 + selectedMinute

            val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hourInitialIndex)
            val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minuteInitialIndex)

            val hourFling = rememberSnapFlingBehavior(lazyListState = hourListState)
            val minuteFling = rememberSnapFlingBehavior(lazyListState = minuteListState)

            // Listen for snap changes
            LaunchedEffect(hourListState) {
                snapshotFlow { hourListState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect { index ->
                        val snappedHour = (index % 12) + 1
                        if (snappedHour != selectedHour12) {
                            selectedHour12 = snappedHour
                            directHourText = String.format("%02d", snappedHour)
                            notifyChanged(snappedHour, selectedMinute, isPm)
                        }
                    }
            }

            LaunchedEffect(minuteListState) {
                snapshotFlow { minuteListState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect { index ->
                        val snappedMin = index % 60
                        if (snappedMin != selectedMinute) {
                            selectedMinute = snappedMin
                            directMinuteText = String.format("%02d", snappedMin)
                            notifyChanged(selectedHour12, snappedMin, isPm)
                        }
                    }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glass selection highlight bar in middle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(itemHeight)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours Wheel
                    LazyColumn(
                        state = hourListState,
                        flingBehavior = hourFling,
                        modifier = Modifier
                            .width(80.dp)
                            .height(height),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(hourItems.size * multiplier) { index ->
                            val hourVal = (index % 12) + 1
                            val isSelected = hourVal == selectedHour12
                            Box(
                                modifier = Modifier
                                    .height(itemHeight)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", hourVal),
                                    fontSize = if (isSelected) 42.sp else 24.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextWhite else TextMuted.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }

                    Text(
                        text = ":",
                        color = TextWhite,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // Minutes Wheel
                    LazyColumn(
                        state = minuteListState,
                        flingBehavior = minuteFling,
                        modifier = Modifier
                            .width(80.dp)
                            .height(height),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(minuteItems.size * multiplier) { index ->
                            val minVal = index % 60
                            val isSelected = minVal == selectedMinute
                            Box(
                                modifier = Modifier
                                    .height(itemHeight)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", minVal),
                                    fontSize = if (isSelected) 42.sp else 24.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextWhite else TextMuted.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // AM / PM Selector
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isPm) AccentAmber.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (!isPm) AccentAmber else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    isPm = false
                                    notifyChanged(selectedHour12, selectedMinute, false)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "AM",
                                color = if (!isPm) AccentAmber else TextMuted,
                                fontSize = 18.sp,
                                fontWeight = if (!isPm) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPm) AccentAmber.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isPm) AccentAmber else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    isPm = true
                                    notifyChanged(selectedHour12, selectedMinute, true)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "PM",
                                color = if (isPm) AccentAmber else TextMuted,
                                fontSize = 18.sp,
                                fontWeight = if (isPm) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Top & Bottom Fading Gradients for 3D wheel effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    com.example.ui.theme.DeepCharcoal,
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    com.example.ui.theme.DeepCharcoal
                                )
                            )
                        )
                )
            }

            Text(
                text = "Tap to type time directly",
                color = AccentViolet,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { isDirectInputMode = true }
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick adjust buttons: +5m, +10m, +15m, +30m, +1h
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val quickButtons = listOf(
                "+5m" to 5,
                "+10m" to 10,
                "+15m" to 15,
                "+30m" to 30,
                "+1h" to 60
            )
            quickButtons.forEach { (label, addMinutes) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            var totalMinutes = (if (isPm) (if (selectedHour12 == 12) 12 else selectedHour12 + 12) else (if (selectedHour12 == 12) 0 else selectedHour12)) * 60 + selectedMinute
                            totalMinutes = (totalMinutes + addMinutes) % (24 * 60)
                            val newH24 = totalMinutes / 60
                            val newMin = totalMinutes % 60
                            isPm = newH24 >= 12
                            selectedHour12 = if (newH24 % 12 == 0) 12 else newH24 % 12
                            selectedMinute = newMin
                            directHourText = String.format("%02d", selectedHour12)
                            directMinuteText = String.format("%02d", selectedMinute)
                            notifyChanged(selectedHour12, selectedMinute, isPm)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
