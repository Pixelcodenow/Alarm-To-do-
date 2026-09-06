package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.data.AlarmEntity
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimelineSheet
import com.example.ui.screens.WakeUpMissionScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentIndigoButton
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.FocusAlarmTheme
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FocusAlarmViewModel

enum class NavigationItem(val title: String, val icon: ImageVector, val tag: String) {
    HOME("HOME", Icons.Default.Home, "nav_home"),
    ALARMS("ALARM", Icons.Default.NotificationsActive, "nav_alarms"),
    FOCUS("FOCUS", Icons.Default.HourglassBottom, "nav_focus"),
    BREATHE("BREATH", Icons.Default.SelfImprovement, "nav_breathe"),
    STATS("STATS", Icons.Default.BarChart, "nav_stats")
}

class MainActivity : ComponentActivity() {
    private val viewModel: FocusAlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            FocusAlarmTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0L)
        if (alarmId > 0) {
            val missionType = intent.getStringExtra(AlarmScheduler.EXTRA_MISSION_TYPE) ?: "MATH"
            val missionCount = intent.getIntExtra(AlarmScheduler.EXTRA_MISSION_COUNT, 3)
            val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Focus Alarm"
            viewModel.activeFiringAlarm.value = AlarmEntity(
                id = alarmId,
                hour = 5,
                minute = 33,
                label = label,
                missionType = missionType,
                missionCount = missionCount
            )
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: FocusAlarmViewModel) {
    var currentTab by remember { mutableStateOf(NavigationItem.HOME) }
    var focusScreenInitialTab by remember { mutableStateOf("POMODORO") }
    var showTimelineSheet by remember { mutableStateOf(false) }
    var showQuickActionSheet by remember { mutableStateOf(false) }

    val activeFiringAlarm by viewModel.activeFiringAlarm.collectAsState()

    // Fullscreen Wake-Up Mission Overlay if an alarm is actively triggering
    if (activeFiringAlarm != null) {
        WakeUpMissionScreen(
            alarm = activeFiringAlarm!!,
            onDismiss = { viewModel.dismissActiveAlarm() }
        )
        return
    }

    // Fullscreen Planner Timeline sheet
    if (showTimelineSheet) {
        TimelineSheet(onClose = { showTimelineSheet = false })
        return
    }

    // Quick Action Dialog
    if (showQuickActionSheet) {
        AlertDialog(
            onDismissRequest = { showQuickActionSheet = false },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "Quick Action",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: Add New Alarm
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                showQuickActionSheet = false
                                currentTab = NavigationItem.ALARMS
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Set New Alarm", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Smart wake & mission challenges", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    // Option 2: Add Priority Task
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                showQuickActionSheet = false
                                viewModel.addTask("Review daily objectives", "Today", "HIGH", "Work")
                                currentTab = NavigationItem.HOME
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Quick Priority Task", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Add to today's action plan", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    // Option 3: Start 25m Focus
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                showQuickActionSheet = false
                                focusScreenInitialTab = "POMODORO"
                                currentTab = NavigationItem.FOCUS
                                viewModel.startPomodoro()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AccentViolet, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Start 25m Deep Work", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Immediate focus session", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    // Option 4: Start 5m Breath
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                showQuickActionSheet = false
                                focusScreenInitialTab = "BREATHE"
                                currentTab = NavigationItem.BREATHE
                                viewModel.startBreathSession()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("5m Box Breathing", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Calm 4-4-4-4 rhythm reset", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickActionSheet = false }) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepCharcoal,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            ProfessionalPolishBottomNav(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    if (tab == NavigationItem.FOCUS) {
                        focusScreenInitialTab = "POMODORO"
                    } else if (tab == NavigationItem.BREATHE) {
                        focusScreenInitialTab = "BREATHE"
                    }
                    currentTab = tab
                },
                onCenterActionClick = { showQuickActionSheet = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            when (currentTab) {
                NavigationItem.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToFocus = {
                            focusScreenInitialTab = "POMODORO"
                            currentTab = NavigationItem.FOCUS
                        },
                        onNavigateToBreath = {
                            focusScreenInitialTab = "BREATHE"
                            currentTab = NavigationItem.BREATHE
                        },
                        onNavigateToAlarms = { currentTab = NavigationItem.ALARMS },
                        onNavigateToTasks = {
                            focusScreenInitialTab = "POMODORO"
                            currentTab = NavigationItem.FOCUS
                        },
                        onOpenTimeline = { showTimelineSheet = true }
                    )
                }

                NavigationItem.ALARMS -> {
                    AlarmsScreen(viewModel = viewModel)
                }

                NavigationItem.FOCUS -> {
                    FocusScreen(
                        viewModel = viewModel,
                        initialTab = "POMODORO"
                    )
                }

                NavigationItem.BREATHE -> {
                    FocusScreen(
                        viewModel = viewModel,
                        initialTab = "BREATHE"
                    )
                }

                NavigationItem.STATS -> {
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * Professional Polish Floating Bottom Navigation Bar
 * Matching: bg-white/5 backdrop-blur-2xl border border-white/10 rounded-full h-16 shadow-lg
 * with center elevated indigo action button (+).
 */
@Composable
fun ProfessionalPolishBottomNav(
    currentTab: NavigationItem,
    onTabSelected: (NavigationItem) -> Unit,
    onCenterActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepCharcoal)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating pill container: bg-white/5 border-white/10 rounded-full
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(50))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(50))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. ALARM
                NavItemView(
                    item = NavigationItem.ALARMS,
                    isSelected = currentTab == NavigationItem.ALARMS,
                    activeColor = AccentAmber,
                    onClick = { onTabSelected(NavigationItem.ALARMS) }
                )

                // 2. FOCUS
                NavItemView(
                    item = NavigationItem.FOCUS,
                    isSelected = currentTab == NavigationItem.FOCUS,
                    activeColor = AccentViolet,
                    onClick = { onTabSelected(NavigationItem.FOCUS) }
                )

                // Center spacer for elevated button
                Spacer(modifier = Modifier.width(48.dp))

                // 3. BREATH
                NavItemView(
                    item = NavigationItem.BREATHE,
                    isSelected = currentTab == NavigationItem.BREATHE,
                    activeColor = AccentCyan,
                    onClick = { onTabSelected(NavigationItem.BREATHE) }
                )

                // 4. STATS
                NavItemView(
                    item = NavigationItem.STATS,
                    isSelected = currentTab == NavigationItem.STATS,
                    activeColor = AccentIndigo,
                    onClick = { onTabSelected(NavigationItem.STATS) }
                )
            }
        }

        // Center Elevated Indigo Button (+):
        // bg-indigo-500 p-3 rounded-full -mt-10 shadow-xl shadow-indigo-500/20 border-4 border-[#0A0A0B]
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(AccentIndigoButton)
                .border(4.dp, DeepCharcoal, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 26.dp),
                    onClick = onCenterActionClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick Action",
                tint = TextWhite,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun NavItemView(
    item: NavigationItem,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(item.tag)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 22.dp),
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) activeColor else TextDisabled,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) activeColor else TextDisabled,
            letterSpacing = 0.5.sp
        )
    }
}
