package com.example.projet.data

import java.time.LocalDate
import java.time.LocalTime

enum class EventType(val label: String) {
    COURS("Cours"),
    TD("TD"),
    TP("TP"),
    EXAM("Exam"),
    AUTRE("Autre")
}

data class Event(
    val id: String,
    val title: String,
    val code: String,
    val location: String,
    val instructor: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val type: EventType
)
