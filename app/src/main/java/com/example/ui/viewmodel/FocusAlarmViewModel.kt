package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FocusAlarmApplication
import com.example.alarm.AlarmScheduler
import com.example.audio.AmbientSound
import com.example.data.AlarmEntity
import com.example.data.BreathSessionEntity
import com.example.data.FocusSessionEntity
import com.example.data.TaskEntity
import com.example.data.UserProfileEntity
import com.example.ui.components.BreathPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class BreathPattern(
    val id: String,
    val name: String,
    val description: String,
    val inhaleSeconds: Int,
    val holdSeconds: Int,
    val exhaleSeconds: Int,
    val postHoldSeconds: Int = 0,
    val defaultDurationMinutes: Int = 5
) {
    val totalCycleSeconds: Int get() = inhaleSeconds + holdSeconds + exhaleSeconds + postHoldSeconds
}

val DefaultBreathPatterns = listOf(
    BreathPattern("calm", "Calm", "Relax your body and mind", 4, 4, 6, 0, 5),
    BreathPattern("box", "Box Breathing", "Focus & composure under stress", 4, 4, 4, 4, 5),
    BreathPattern("relax", "Relax", "Wind down to release muscle tension", 4, 0, 6, 0, 3),
    BreathPattern("deep", "Deep Breathing", "Slow practice to increase lung capacity", 5, 2, 7, 0, 10),
    BreathPattern("sleep", "Sleep", "Bedtime prep for drifting off easily", 4, 6, 8, 0, 15),
    BreathPattern("custom", "Custom (FocusFlow)", "My custom pattern", 4, 2, 4, 2, 5)
)

enum class PomodoroMode(val displayName: String, val defaultMinutes: Int) {
    DEEP_WORK("Deep Work", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

class FocusAlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FocusAlarmApplication
    private val repository = app.repository
    private val audioEngine = app.audioEngine

    // Room Flows
    val allAlarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<TaskEntity>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<TaskEntity>> = repository.completedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Next Alarm calculation
    val nextAlarm = allAlarms.combine(MutableStateFlow(System.currentTimeMillis())) { alarms, _ ->
        val enabled = alarms.filter { it.isEnabled }
        if (enabled.isEmpty()) null
        else {
            enabled.minByOrNull {
                AlarmScheduler.calculateNextTriggerTime(it.hour, it.minute, it.repeatDays)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Alarm firing state (for Wake-Up Mission)
    val activeFiringAlarm = MutableStateFlow<AlarmEntity?>(null)

    // ==========================================
    // POMODORO TIMER STATE
    // ==========================================
    private val _pomodoroMode = MutableStateFlow(PomodoroMode.DEEP_WORK)
    val pomodoroMode = _pomodoroMode.asStateFlow()

    private val _pomodoroTotalSeconds = MutableStateFlow(25 * 60)
    val pomodoroTotalSeconds = _pomodoroTotalSeconds.asStateFlow()

    private val _pomodoroRemainingSeconds = MutableStateFlow(25 * 60)
    val pomodoroRemainingSeconds = _pomodoroRemainingSeconds.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning = _isPomodoroRunning.asStateFlow()

    private val _isPomodoroPaused = MutableStateFlow(false)
    val isPomodoroPaused = _isPomodoroPaused.asStateFlow()

    private val _attachedTask = MutableStateFlow<TaskEntity?>(null)
    val attachedTask = _attachedTask.asStateFlow()

    private val _pomodoroSessionCycle = MutableStateFlow(3) // 1..4
    val pomodoroSessionCycle = _pomodoroSessionCycle.asStateFlow()

    private var pomodoroJob: Job? = null
    private var pomodoroEndTimestamp: Long = 0L

    // ==========================================
    // BREATHING EXERCISE STATE
    // ==========================================
    private val _selectedBreathPattern = MutableStateFlow(DefaultBreathPatterns[0])
    val selectedBreathPattern = _selectedBreathPattern.asStateFlow()

    private val _breathSessionDurationMinutes = MutableStateFlow(5)
    val breathSessionDurationMinutes = _breathSessionDurationMinutes.asStateFlow()

    private val _isBreathActive = MutableStateFlow(false)
    val isBreathActive = _isBreathActive.asStateFlow()

    private val _isBreathPaused = MutableStateFlow(false)
    val isBreathPaused = _isBreathPaused.asStateFlow()

    private val _isBreathCompleted = MutableStateFlow(false)
    val isBreathCompleted = _isBreathCompleted.asStateFlow()

    private val _currentBreathPhase = MutableStateFlow(BreathPhase.INHALE)
    val currentBreathPhase = _currentBreathPhase.asStateFlow()

    private val _phaseSecondsRemaining = MutableStateFlow(4)
    val phaseSecondsRemaining = _phaseSecondsRemaining.asStateFlow()

    private val _phaseFraction = MutableStateFlow(0f)
    val phaseFraction = _phaseFraction.asStateFlow()

    private val _breathTotalRemainingSeconds = MutableStateFlow(300)
    val breathTotalRemainingSeconds = _breathTotalRemainingSeconds.asStateFlow()

    private val _breathCompletedCycles = MutableStateFlow(0)
    val breathCompletedCycles = _breathCompletedCycles.asStateFlow()

    private val _breathTotalCycles = MutableStateFlow(12)
    val breathTotalCycles = _breathTotalCycles.asStateFlow()

    private val _selectedAmbientSound = MutableStateFlow(AmbientSound.RAIN)
    val selectedAmbientSound = _selectedAmbientSound.asStateFlow()

    private val _ambientVolume = MutableStateFlow(0.75f)
    val ambientVolume = _ambientVolume.asStateFlow()

    private val _chimeVolume = MutableStateFlow(0.85f)
    val chimeVolume = _chimeVolume.asStateFlow()

    private var breathJob: Job? = null

    // ==========================================
    // ALARMS ACTIONS
    // ==========================================
    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val id = if (alarm.id == 0L) {
                repository.insertAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
                alarm.id
            }
            val savedAlarm = alarm.copy(id = id)
            AlarmScheduler.scheduleAlarm(app, savedAlarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                AlarmScheduler.scheduleAlarm(app, updated)
            } else {
                AlarmScheduler.cancelAlarm(app, updated.id)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(app, alarm.id)
            repository.deleteAlarmById(alarm.id)
        }
    }

    fun triggerAlarmMissionTest(alarm: AlarmEntity) {
        activeFiringAlarm.value = alarm
    }

    fun dismissActiveAlarm() {
        activeFiringAlarm.value = null
    }

    // ==========================================
    // TASKS ACTIONS
    // ==========================================
    fun addTask(title: String, dueDate: String, priority: String, tag: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    dueDate = dueDate,
                    priority = priority,
                    tag = tag
                )
            )
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            repository.setTaskCompleted(task.id, newCompleted)
            if (_attachedTask.value?.id == task.id && newCompleted) {
                // If currently attached task is completed, update attached task
                _attachedTask.value = task.copy(isCompleted = true)
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            if (_attachedTask.value?.id == taskId) {
                _attachedTask.value = null
            }
            repository.deleteTaskById(taskId)
        }
    }

    fun attachTaskToPomodoro(task: TaskEntity?) {
        _attachedTask.value = task
    }

    // ==========================================
    // POMODORO TIMER LOGIC (Timestamp based)
    // ==========================================
    fun setPomodoroMode(mode: PomodoroMode) {
        if (_isPomodoroRunning.value) {
            resetPomodoro()
        }
        _pomodoroMode.value = mode
        val secs = mode.defaultMinutes * 60
        _pomodoroTotalSeconds.value = secs
        _pomodoroRemainingSeconds.value = secs
    }

    fun startPomodoro() {
        if (_isPomodoroRunning.value && !_isPomodoroPaused.value) return
        _isPomodoroRunning.value = true
        _isPomodoroPaused.value = false

        pomodoroEndTimestamp = SystemClock.elapsedRealtime() + (_pomodoroRemainingSeconds.value * 1000L)

        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (isActive && _isPomodoroRunning.value && !_isPomodoroPaused.value) {
                val remainingMs = pomodoroEndTimestamp - SystemClock.elapsedRealtime()
                val remainingSecs = (remainingMs / 1000L).toInt().coerceAtLeast(0)
                _pomodoroRemainingSeconds.value = remainingSecs

                if (remainingSecs <= 0) {
                    // Session Completed!
                    audioEngine.playBellChime(528.0)
                    completePomodoroSession()
                    break
                }
                delay(250)
            }
        }
    }

    fun pausePomodoro() {
        if (_isPomodoroRunning.value && !_isPomodoroPaused.value) {
            _isPomodoroPaused.value = true
            pomodoroJob?.cancel()
            pomodoroJob = null
        }
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        pomodoroJob = null
        _isPomodoroRunning.value = false
        _isPomodoroPaused.value = false
        _pomodoroRemainingSeconds.value = _pomodoroTotalSeconds.value
    }

    fun skipPomodoro() {
        resetPomodoro()
        val nextMode = when (_pomodoroMode.value) {
            PomodoroMode.DEEP_WORK -> {
                if (_pomodoroSessionCycle.value >= 4) PomodoroMode.LONG_BREAK else PomodoroMode.SHORT_BREAK
            }
            PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> PomodoroMode.DEEP_WORK
        }
        setPomodoroMode(nextMode)
    }

    private fun completePomodoroSession() {
        val completedMode = _pomodoroMode.value
        val task = _attachedTask.value
        val durationMins = _pomodoroTotalSeconds.value / 60

        viewModelScope.launch {
            repository.recordFocusSession(
                taskTitle = task?.title ?: "Deep Work Session",
                durationMinutes = durationMins,
                mode = completedMode.name,
                tag = task?.tag ?: "Work",
                taskId = task?.id
            )
        }

        resetPomodoro()
        if (completedMode == PomodoroMode.DEEP_WORK) {
            val nextCycle = (_pomodoroSessionCycle.value % 4) + 1
            _pomodoroSessionCycle.value = nextCycle
            val nextMode = if (nextCycle == 1) PomodoroMode.LONG_BREAK else PomodoroMode.SHORT_BREAK
            setPomodoroMode(nextMode)
        } else {
            setPomodoroMode(PomodoroMode.DEEP_WORK)
        }
    }

    // ==========================================
    // BREATHING LOGIC & AUDIO SYNCHRONIZATION
    // ==========================================
    fun selectBreathPattern(pattern: BreathPattern) {
        if (_isBreathActive.value) stopBreathSession()
        _selectedBreathPattern.value = pattern
        _breathSessionDurationMinutes.value = pattern.defaultDurationMinutes
        _breathTotalRemainingSeconds.value = pattern.defaultDurationMinutes * 60
        _breathTotalCycles.value = ((pattern.defaultDurationMinutes * 60) / pattern.totalCycleSeconds).coerceAtLeast(1)
    }

    fun setBreathDurationMinutes(minutes: Int) {
        _breathSessionDurationMinutes.value = minutes
        _breathTotalRemainingSeconds.value = minutes * 60
        val pattern = _selectedBreathPattern.value
        _breathTotalCycles.value = ((minutes * 60) / pattern.totalCycleSeconds).coerceAtLeast(1)
    }

    fun saveCustomPattern(inhale: Int, hold: Int, exhale: Int, postHold: Int, durationMins: Int) {
        val custom = BreathPattern(
            id = "custom",
            name = "Custom (FocusFlow)",
            description = "My custom pattern",
            inhaleSeconds = inhale,
            holdSeconds = hold,
            exhaleSeconds = exhale,
            postHoldSeconds = postHold,
            defaultDurationMinutes = durationMins
        )
        selectBreathPattern(custom)
    }

    fun startBreathSession() {
        val pattern = _selectedBreathPattern.value
        _isBreathActive.value = true
        _isBreathPaused.value = false
        _isBreathCompleted.value = false
        _breathCompletedCycles.value = 0

        val totalSec = _breathSessionDurationMinutes.value * 60
        _breathTotalRemainingSeconds.value = totalSec
        _breathTotalCycles.value = (totalSec / pattern.totalCycleSeconds).coerceAtLeast(1)

        // Start ambient sound
        audioEngine.ambientVolume = _ambientVolume.value
        audioEngine.chimeVolume = _chimeVolume.value
        audioEngine.play(_selectedAmbientSound.value)

        breathJob?.cancel()
        breathJob = viewModelScope.launch {
            runBreathingCycleLoop()
        }
    }

    fun pauseBreathSession() {
        if (_isBreathActive.value && !_isBreathPaused.value) {
            _isBreathPaused.value = true
            audioEngine.pause()
        }
    }

    fun resumeBreathSession() {
        if (_isBreathActive.value && _isBreathPaused.value) {
            _isBreathPaused.value = false
            audioEngine.resume()
        }
    }

    fun stopBreathSession() {
        breathJob?.cancel()
        breathJob = null
        _isBreathActive.value = false
        _isBreathPaused.value = false
        _isBreathCompleted.value = false
        audioEngine.stop()
    }

    fun previewSound(sound: AmbientSound) {
        _selectedAmbientSound.value = sound
        audioEngine.preview(sound, 3500L)
    }

    fun setAmbientSound(sound: AmbientSound) {
        _selectedAmbientSound.value = sound
        if (_isBreathActive.value && !_isBreathPaused.value) {
            audioEngine.play(sound)
        }
    }

    fun setAmbientVolume(vol: Float) {
        _ambientVolume.value = vol
        audioEngine.ambientVolume = vol
    }

    fun setChimeVolume(vol: Float) {
        _chimeVolume.value = vol
        audioEngine.chimeVolume = vol
    }

    private suspend fun runBreathingCycleLoop() {
        val pattern = _selectedBreathPattern.value
        var remainingTotal = _breathTotalRemainingSeconds.value
        var completedCycles = 0

        while (viewModelScope.isActive && _isBreathActive.value && remainingTotal > 0) {
            // PHASE 1: INHALE
            audioEngine.playBellChime(440.0) // Gentle chime on inhale start
            _currentBreathPhase.value = BreathPhase.INHALE
            for (sec in pattern.inhaleSeconds downTo 1) {
                while (_isBreathPaused.value) delay(100)
                _phaseSecondsRemaining.value = sec
                val fraction = 1f - (sec.toFloat() / pattern.inhaleSeconds.toFloat())
                _phaseFraction.value = fraction
                delay(1000)
                remainingTotal--
                _breathTotalRemainingSeconds.value = remainingTotal
                if (remainingTotal <= 0) break
            }
            if (remainingTotal <= 0) break

            // PHASE 2: HOLD (if > 0)
            if (pattern.holdSeconds > 0) {
                _currentBreathPhase.value = BreathPhase.HOLD
                for (sec in pattern.holdSeconds downTo 1) {
                    while (_isBreathPaused.value) delay(100)
                    _phaseSecondsRemaining.value = sec
                    _phaseFraction.value = 1f
                    delay(1000)
                    remainingTotal--
                    _breathTotalRemainingSeconds.value = remainingTotal
                    if (remainingTotal <= 0) break
                }
            }
            if (remainingTotal <= 0) break

            // PHASE 3: EXHALE
            audioEngine.playBellChime(330.0) // Gentle lower chime on exhale start
            _currentBreathPhase.value = BreathPhase.EXHALE
            for (sec in pattern.exhaleSeconds downTo 1) {
                while (_isBreathPaused.value) delay(100)
                _phaseSecondsRemaining.value = sec
                val fraction = 1f - (sec.toFloat() / pattern.exhaleSeconds.toFloat())
                _phaseFraction.value = fraction
                delay(1000)
                remainingTotal--
                _breathTotalRemainingSeconds.value = remainingTotal
                if (remainingTotal <= 0) break
            }
            if (remainingTotal <= 0) break

            // PHASE 4: POST-HOLD (if > 0)
            if (pattern.postHoldSeconds > 0) {
                _currentBreathPhase.value = BreathPhase.HOLD_POST
                for (sec in pattern.postHoldSeconds downTo 1) {
                    while (_isBreathPaused.value) delay(100)
                    _phaseSecondsRemaining.value = sec
                    _phaseFraction.value = 0f
                    delay(1000)
                    remainingTotal--
                    _breathTotalRemainingSeconds.value = remainingTotal
                    if (remainingTotal <= 0) break
                }
            }

            completedCycles++
            _breathCompletedCycles.value = completedCycles
        }

        // Finish session!
        audioEngine.stop()
        audioEngine.playBellChime(528.0)
        _isBreathCompleted.value = true

        val totalDurationSec = (_breathSessionDurationMinutes.value * 60) - remainingTotal.coerceAtLeast(0)
        val avgBreath = if (completedCycles > 0) totalDurationSec / completedCycles else pattern.totalCycleSeconds

        viewModelScope.launch {
            repository.recordBreathSession(
                patternName = pattern.name,
                durationSeconds = totalDurationSec,
                completedCycles = completedCycles,
                avgBreathSeconds = avgBreath,
                soundName = _selectedAmbientSound.value.displayName
            )
        }
    }

    // ==========================================
    // PROFILE & SETTINGS
    // ==========================================
    fun updateThemeAccent(accent: String) {
        viewModelScope.launch {
            val prof = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(prof.copy(accentTheme = accent))
        }
    }

    fun toggleHapticFeedback() {
        viewModelScope.launch {
            val prof = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(prof.copy(hapticFeedback = !prof.hapticFeedback))
        }
    }

    fun toggleSmartAlarmReminders() {
        viewModelScope.launch {
            val prof = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(prof.copy(smartAlarmReminders = !prof.smartAlarmReminders))
        }
    }

    fun toggleFocusDailySummary() {
        viewModelScope.launch {
            val prof = userProfile.value ?: UserProfileEntity()
            repository.updateProfile(prof.copy(focusDailySummary = !prof.focusDailySummary))
        }
    }

    fun clearFocusHistory() {
        viewModelScope.launch {
            repository.clearFocusHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        pomodoroJob?.cancel()
        breathJob?.cancel()
        audioEngine.stop()
    }
}
