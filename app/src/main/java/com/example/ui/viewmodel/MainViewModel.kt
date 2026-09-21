package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmHistoryEntity
import com.example.data.repository.AlarmRepository
import com.example.data.repository.AlarmStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = AlarmRepository(application)

  val alarms: StateFlow<List<AlarmEntity>> = repository.allAlarmsFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val history: StateFlow<List<AlarmHistoryEntity>> = repository.historyFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _stats = MutableStateFlow(AlarmStats())
  val stats: StateFlow<AlarmStats> = _stats.asStateFlow()

  init {
    refreshStats()
    viewModelScope.launch {
      repository.historyFlow.collect {
        refreshStats()
      }
    }
  }

  fun refreshStats() {
    viewModelScope.launch {
      _stats.value = repository.calculateStats()
    }
  }

  fun toggleAlarm(alarm: AlarmEntity) {
    viewModelScope.launch {
      repository.toggleAlarm(alarm)
    }
  }

  fun saveAlarm(alarm: AlarmEntity) {
    viewModelScope.launch {
      if (alarm.id == 0L) {
        repository.insertAlarm(alarm)
      } else {
        repository.updateAlarm(alarm)
      }
    }
  }

  fun deleteAlarm(alarm: AlarmEntity) {
    viewModelScope.launch {
      repository.deleteAlarm(alarm)
    }
  }

  fun clearHistory() {
    viewModelScope.launch {
      repository.clearHistory()
      refreshStats()
    }
  }

  fun getNextAlarm(alarmsList: List<AlarmEntity>): Pair<AlarmEntity, Long>? {
    val enabled = alarmsList.filter { it.isEnabled }
    if (enabled.isEmpty()) return null

    var earliestAlarm: AlarmEntity? = null
    var earliestTime = Long.MAX_VALUE

    for (a in enabled) {
      val t = AlarmScheduler.calculateNextTriggerTime(a.hour, a.minute, a.getDaysList())
      if (t < earliestTime) {
        earliestTime = t
        earliestAlarm = a
      }
    }

    return if (earliestAlarm != null) Pair(earliestAlarm, earliestTime) else null
  }
}
