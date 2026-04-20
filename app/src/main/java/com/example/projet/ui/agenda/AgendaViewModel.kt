package com.example.projet.ui.agenda

import androidx.lifecycle.ViewModel
import com.example.projet.data.Event
import com.example.projet.data.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class AgendaViewModel : ViewModel() {

    private val _weekStart = MutableStateFlow(weekOf(LocalDate.now()))
    val weekStart: StateFlow<LocalDate> = _weekStart.asStateFlow()

    private val _selectedDate = MutableStateFlow(firstDayWithEvents(weekOf(LocalDate.now())))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    init { refreshEvents() }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        refreshEvents()
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
        _selectedDate.value = _weekStart.value
        refreshEvents()
    }

    fun prevWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
        _selectedDate.value = _weekStart.value
        refreshEvents()
    }

    private fun refreshEvents() {
        _events.value = SampleData.getEventsForDate(_selectedDate.value)
    }

    private fun weekOf(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private fun firstDayWithEvents(weekStart: LocalDate): LocalDate {
        for (i in 0..4) {
            val d = weekStart.plusDays(i.toLong())
            if (SampleData.getEventsForDate(d).isNotEmpty()) return d
        }
        return weekStart
    }
}
