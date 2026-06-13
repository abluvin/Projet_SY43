package com.example.projet.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.Event
import com.example.projet.data.SampleData
import com.example.projet.data.repository.EventRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = EventRepository(
        (application as ProjetApplication).database.eventDao()
    )

    private val _weekStart = MutableStateFlow(weekOf(LocalDate.now()))
    val weekStart: StateFlow<LocalDate> = _weekStart.asStateFlow()

    private val _selectedDate = MutableStateFlow(_weekStart.value)
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<Event>> = selectedDate
        .flatMapLatest { date -> repo.getByDate(date.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (repo.getAll().first().isEmpty()) {
                repo.insertAll(SampleData.events)
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
        _selectedDate.value = _weekStart.value
    }

    fun prevWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
        _selectedDate.value = _weekStart.value
    }

    fun addEvents(newEvents: List<Event>) {
        viewModelScope.launch { repo.insertAll(newEvents) }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch { repo.insert(event) }
    }

    private fun weekOf(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
