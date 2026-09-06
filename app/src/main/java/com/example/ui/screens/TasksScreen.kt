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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
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
fun TasksScreen(
    viewModel: FocusAlarmViewModel,
    onStartFocusWithTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()

    var filterSelected by remember { mutableStateOf("All") } // "All", "Today", "This Week", "Completed"
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Dialog for adding task
    if (showAddTaskDialog) {
        var newTaskTitle by remember { mutableStateOf("") }
        var newTaskDueDate by remember { mutableStateOf("Today") }
        var newTaskPriority by remember { mutableStateOf("MEDIUM") }
        var newTaskTag by remember { mutableStateOf("Work") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = DeepCharcoal,
            title = {
                Text(
                    text = "New Focus Task",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BasicTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        textStyle = TextStyle(color = TextWhite, fontSize = 15.sp),
                        cursorBrush = SolidColor(AccentViolet),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalElevated)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                    .padding(14.dp)
                            ) {
                                if (newTaskTitle.isEmpty()) {
                                    Text("Task title...", color = TextMuted, fontSize = 15.sp)
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Due date selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Today", "Tomorrow", "This Week").forEach { due ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (newTaskDueDate == due) AccentViolet else GlassSurface)
                                    .clickable { newTaskDueDate = due }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = due,
                                    color = if (newTaskDueDate == due) DeepCharcoal else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Priority selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("HIGH" to AccentCoral, "MEDIUM" to AccentAmber, "LOW" to AccentMint).forEach { (p, col) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (newTaskPriority == p) col.copy(alpha = 0.2f) else GlassSurface)
                                    .border(1.dp, if (newTaskPriority == p) col else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { newTaskPriority = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p,
                                    color = if (newTaskPriority == p) col else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTask(newTaskTitle.trim(), newTaskDueDate, newTaskPriority, newTaskTag)
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("Add Task", color = AccentViolet, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header (matches screen-5-tasks.png)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tasks",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentViolet)
                            .clickable { showAddTaskDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = DeepCharcoal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Filter Chips: All, Today, This Week, Completed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Today", "This Week", "Completed")
                    filters.forEach { filter ->
                        val isSelected = filterSelected == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) AccentViolet else GlassSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentViolet else GlassBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { filterSelected = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) DeepCharcoal else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // IN PROGRESS SECTION
            if (filterSelected != "Completed") {
                val filteredActive = when (filterSelected) {
                    "Today" -> activeTasks.filter { it.dueDate == "Today" }
                    "This Week" -> activeTasks.filter { it.dueDate == "Today" || it.dueDate == "Tomorrow" || it.dueDate == "This Week" }
                    else -> activeTasks
                }

                item {
                    Text(
                        text = "IN PROGRESS (${filteredActive.size})",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(filteredActive.size) { index ->
                    val task = filteredActive[index]
                    TaskItemCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskComplete(task) },
                        onDelete = { viewModel.deleteTask(task.id) },
                        onStartFocus = {
                            viewModel.attachTaskToPomodoro(task)
                            onStartFocusWithTask(task)
                        }
                    )
                }
            }

            // COMPLETED SECTION
            if (filterSelected == "All" || filterSelected == "Completed") {
                item {
                    Text(
                        text = "COMPLETED (${completedTasks.size})",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                items(completedTasks.size) { index ->
                    val task = completedTasks[index]
                    TaskItemCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskComplete(task) },
                        onDelete = { viewModel.deleteTask(task.id) },
                        onStartFocus = {}
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Checkbox: toggle complete (matching Professional Polish rounded-md indigo style)
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (task.isCompleted) com.example.ui.theme.AccentIndigoButton else Color.Transparent)
                        .border(
                            1.dp,
                            if (task.isCompleted) com.example.ui.theme.AccentIndigoButton else Color(0x33FFFFFF),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = TextWhite,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) TextDisabled else TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val priorityColor = when (task.priority) {
                            "HIGH" -> AccentCoral
                            "MEDIUM" -> AccentAmber
                            else -> AccentMint
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(priorityColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${task.dueDate} • ${task.tag}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Right actions: Start Focus & Delete (Distinct action for delete!)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!task.isCompleted) {
                    IconButton(onClick = onStartFocus) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = "Start Focus",
                            tint = AccentViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Delete Action (Distinct from complete, permanently removes)
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
