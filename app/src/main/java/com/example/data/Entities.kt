package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val label: String,
    val isEnabled: Boolean = true,
    val repeatDays: String = "1,2,3,4,5", // 0=Sun, 1=Mon, ..., 6=Sat
    val missionType: String = "MATH", // MATH, SHAKE, MEMORY, PHRASE, QR, MOVEMENT
    val missionDifficulty: String = "EASY", // EASY, MEDIUM, HARD
    val missionCount: Int = 3,
    val soundName: String = "Zen Forest",
    val isVibrationEnabled: Boolean = true,
    val isSnoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 3,
    val snoozeMaxTimes: Int = 3,
    val isSmartWake: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val dueDate: String = "Today", // Today, Tomorrow, This Week, Later
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val tag: String = "Work", // Work, Study, Self, General
    val estimatedSessions: Int = 2,
    val completedSessions: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int = 25,
    val completedAt: Long = System.currentTimeMillis(),
    val mode: String = "DEEP_WORK", // DEEP_WORK, SHORT_BREAK, LONG_BREAK
    val tag: String = "Work"
)

@Entity(tableName = "breath_sessions")
data class BreathSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patternName: String = "Calm",
    val durationSeconds: Int = 300,
    val completedCycles: Int = 12,
    val avgBreathSeconds: Int = 14,
    val completedAt: Long = System.currentTimeMillis(),
    val soundName: String = "Rain"
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Alex Chen",
    val email: String = "alex.chen@focusalarm.io",
    val isPro: Boolean = true,
    val smartAlarmReminders: Boolean = true,
    val focusDailySummary: Boolean = false,
    val accentTheme: String = "VIOLET", // VIOLET, AMBER, MINT
    val hapticFeedback: Boolean = true,
    val streakDays: Int = 14
)
