package com.example.projet.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.Event
import com.example.projet.data.SampleData
import com.example.projet.data.repository.EventRepository
import com.example.projet.data.repository.UERepository
import com.example.projet.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: EventRepository
    private val ueRepo: UERepository
    private val userRepo: UserRepository

    init {
        val db = (application as ProjetApplication).database
        repo = EventRepository(db.eventDao())
        ueRepo = UERepository(db.ueDao(), db.userUEDao())
        userRepo = UserRepository(db.userDao())
        viewModelScope.launch {
            if (repo.getAll().first().isEmpty()) {
                repo.insertAll(SampleData.events)
            }
        }
    }

    private val _weekStart = MutableStateFlow(weekOf(LocalDate.now()))
    val weekStart: StateFlow<LocalDate> = _weekStart.asStateFlow()

    private val _selectedDate = MutableStateFlow(_weekStart.value)
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _userId = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userUECodes: StateFlow<List<String>> = _userId
        .flatMapLatest { uid ->
            if (uid == 0) flowOf(emptyList())
            else ueRepo.getUserUEs(uid).map { items -> items.map { it.ue.code } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userBranch: StateFlow<String> = _userId
        .flatMapLatest { uid ->
            if (uid == 0) flowOf("")
            else flow { emit(userRepo.getById(uid)?.branch ?: "") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val allUECodes: StateFlow<List<String>> = ueRepo.getAllUEs()
        .map { ues -> ues.map { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<Event>> = combine(selectedDate, userUECodes, userBranch) { date, codes, branch ->
        Triple(date, codes, branch)
    }.flatMapLatest { (date, codes, branch) ->
        repo.getByDate(date.toString()).map { list -> list.filter { isVisibleForUser(it, codes, branch) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val datesWithEvents: StateFlow<Set<LocalDate>> = combine(_weekStart, userUECodes, userBranch) { start, codes, branch ->
        Triple(start, codes, branch)
    }.flatMapLatest { (start, codes, branch) ->
        val end = start.plusDays(6)
        repo.getByDateRange(start.toString(), end.toString()).map { list ->
            list.filter { isVisibleForUser(it, codes, branch) }.map { it.date }.toSet()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setUserId(userId: Int) {
        if (_userId.value != userId) _userId.value = userId
    }

    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
        _selectedDate.value = _weekStart.value
    }

    fun prevWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
        _selectedDate.value = _weekStart.value
    }

    fun addEvents(newEvents: List<Event>) { viewModelScope.launch { repo.insertAll(newEvents) } }

    fun addEvent(event: Event) { viewModelScope.launch { repo.insert(event) } }

    private fun isVisibleForUser(event: Event, userUECodes: List<String>, userBranch: String): Boolean {
        if (event.isGlobal) return true
        if (event.targetUECodes.isEmpty() && event.targetBranches.isEmpty()) return true
        if (event.targetUECodes.any { it in userUECodes }) return true
        if (userBranch.isNotBlank() && event.targetBranches.contains(userBranch)) return true
        return false
    }

    private fun weekOf(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
