package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FocusAlarmViewModel

@Composable
fun ProfileScreen(
    viewModel: FocusAlarmViewModel,
    initialTab: String = "INSIGHTS", // "INSIGHTS" or "PROFILE"
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val profile by viewModel.userProfile.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = DeepCharcoal,
            title = {
                Text("Clear Focus History", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete all recorded focus session statistics? This cannot be undone.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearFocusHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = AccentCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Segmented Switch: Insights vs Profile (matches screen-6-profile-insights.png)
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
                    .background(if (selectedTab == "INSIGHTS") AccentViolet else Color.Transparent)
                    .clickable { selectedTab = "INSIGHTS" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Insights",
                    color = if (selectedTab == "INSIGHTS") DeepCharcoal else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == "PROFILE") AccentViolet else Color.Transparent)
                    .clickable { selectedTab = "PROFILE" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Profile",
                    color = if (selectedTab == "PROFILE") DeepCharcoal else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == "INSIGHTS") {
            InsightsTabContent(focusSessionsCount = focusSessions.size)
        } else {
            ProfileTabContent(
                profile = profile,
                onToggleSmartAlarm = { viewModel.toggleSmartAlarmReminders() },
                onToggleDailySummary = { viewModel.toggleFocusDailySummary() },
                onToggleHaptic = { viewModel.toggleHapticFeedback() },
                onSelectAccent = { viewModel.updateThemeAccent(it) },
                onClearHistory = { showClearHistoryDialog = true }
            )
        }
    }
}

@Composable
fun InsightsTabContent(focusSessionsCount: Int) {
    var period by remember { mutableStateOf("Week") } // "Week" or "Month"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Week / Month selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .padding(3.dp)
                ) {
                    listOf("Week", "Month").forEach { p ->
                        val isSelected = period == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentViolet else Color.Transparent)
                                .clickable { period = p }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = p,
                                color = if (isSelected) DeepCharcoal else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // WEEKLY FOCUS TIME (matches screen-6-profile-insights.png)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "WEEKLY FOCUS TIME",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "26.8 hours total",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bar Chart: Mon, Tue, Wed, Thu, Fri, Sat, Sun
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val heights = listOf(0.45f, 0.65f, 1.0f, 0.70f, 0.55f, 0.30f, 0.20f)
                    val chartHeight = 120.dp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEachIndexed { idx, day ->
                            val hFraction = heights[idx]
                            val isPeak = idx == 2 // Wed is peak
                            val isToday = idx == 3 // Thu is today

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .fillMaxHeight(hFraction)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            if (isPeak) AccentViolet
                                            else if (isToday) AccentAmber
                                            else CharcoalElevated
                                        )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = day,
                                    color = if (isToday) AccentAmber else if (isPeak) AccentViolet else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday || isPeak) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4 METRIC CARDS (matches screen-6-profile-insights.png)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Average Focus", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("3.8h/day", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Best Focus Day", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Wednesday", color = AccentViolet, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Sleep Quality", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("88% (Optimal)", color = AccentMint, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Streak Record", color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("24 Days", color = AccentAmber, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // FOCUS DISTRIBUTION (matches screen-6-profile-insights.png)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FOCUS DISTRIBUTION",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Segmented horizontal progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.60f)
                                .fillMaxHeight()
                                .background(AccentViolet)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.25f)
                                .fillMaxHeight()
                                .background(AccentCyan)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.15f)
                                .fillMaxHeight()
                                .background(AccentMint)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendItem("Work 60%", AccentViolet)
                        LegendItem("Study 25%", AccentCyan)
                        LegendItem("Self 15%", AccentMint)
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
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = TextWhite, fontSize = 12.sp)
    }
}

@Composable
fun ProfileTabContent(
    profile: com.example.data.UserProfileEntity?,
    onToggleSmartAlarm: () -> Unit,
    onToggleDailySummary: () -> Unit,
    onToggleHaptic: () -> Unit,
    onSelectAccent: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card (matches screen-6-profile-insights.png)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AccentViolet, AccentAmber)
                                )
                            )
                            .border(1.5.dp, GlassBorder, CircleShape)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(CharcoalElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AC",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.name ?: "Alex Chen",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentAmber)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    color = DeepCharcoal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile?.email ?: "alex.chen@focusalarm.io",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // NOTIFICATION PREFERENCES
        item {
            Text(
                text = "NOTIFICATION PREFERENCES",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Smart Alarm Reminders", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Evening prompt to set wake-up goal", color = TextMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = profile?.smartAlarmReminders ?: true,
                            onCheckedChange = { onToggleSmartAlarm() },
                            colors = SwitchDefaults.colors(checkedThumbColor = DeepCharcoal, checkedTrackColor = AccentViolet)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Focus Daily Summary", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Daily insights and completed tasks", color = TextMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = profile?.focusDailySummary ?: false,
                            onCheckedChange = { onToggleDailySummary() },
                            colors = SwitchDefaults.colors(checkedThumbColor = DeepCharcoal, checkedTrackColor = AccentViolet)
                        )
                    }
                }
            }
        }

        // APPEARANCE & STYLE
        item {
            Text(
                text = "APPEARANCE & STYLE",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Focus Accent Theme", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Glass glow and primary controls", color = TextMuted, fontSize = 12.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "VIOLET" to AccentViolet,
                                "AMBER" to AccentAmber,
                                "MINT" to AccentMint
                            ).forEach { (name, color) ->
                                val isSelected = (profile?.accentTheme ?: "VIOLET") == name
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            2.dp,
                                            if (isSelected) TextWhite else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { onSelectAccent(name) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Vibration Feedback", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Subtle response for mission taps", color = TextMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = profile?.hapticFeedback ?: true,
                            onCheckedChange = { onToggleHaptic() },
                            colors = SwitchDefaults.colors(checkedThumbColor = DeepCharcoal, checkedTrackColor = AccentViolet)
                        )
                    }
                }
            }
        }

        // GENERAL & PRIVACY
        item {
            Text(
                text = "GENERAL & PRIVACY",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = AccentMint, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Cloud Sync Backups", color = TextWhite, fontSize = 15.sp)
                        }
                        Text("Enabled", color = AccentMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClearHistory() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Clear Focus History", color = AccentCoral, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FocusAlarm v2.4.0 • Clean Production Build",
                    color = TextMuted.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
