package com.example.projet.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object SampleData {

    // Semaine du 20 avril 2026 (lundi)
    private val semaine1 = LocalDate.of(2026, 4, 20)
    private val semaine2 = semaine1.plusWeeks(1)

    val events: List<Event> = listOf(
        // --- Semaine 1 : 20–24 avril ---
        // Lundi
        Event("1", "SY43", "SY43", "I102", "M. A",
            semaine1, LocalTime.of(8, 0), LocalTime.of(10, 0), EventType.COURS),
        Event("2", "SI40", "SI40", "A104", "Mme. B",
            semaine1, LocalTime.of(14, 0), LocalTime.of(16, 0), EventType.TD),

        // Mardi
        Event("3", "WE4A", "WE4A", "A201", "M. C",
            semaine1.plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0), EventType.COURS),
        Event("4", "LE09 - Anglais", "LE09", "A201", "Mme. D",
            semaine1.plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0), EventType.TD),

        // Mercredi
        Event("5", "RS40", "LO43", "A202", "M. D",
            semaine1.plusDays(2), LocalTime.of(8, 0), LocalTime.of(10, 0), EventType.COURS),
        Event("6", "SI40 – TP", "SI40", "A202", "M. A",
            semaine1.plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0), EventType.TP),

        // Jeudi
        Event("7", "SY43 – TP", "SY43", "A101", "M. Z",
            semaine1.plusDays(3), LocalTime.of(8, 0), LocalTime.of(10, 0), EventType.TP),
        Event("8", "SI40 – TD ", "SI40", "A201", "M. E",
            semaine1.plusDays(3), LocalTime.of(14, 0), LocalTime.of(16, 0), EventType.TD),

        // Vendredi
        Event("9", "SY43 – TD", "SY43", "A104", "Mme. B",
            semaine1.plusDays(4), LocalTime.of(8, 0), LocalTime.of(10, 0), EventType.COURS),
        Event("10", "WE4A – Contrôle", "WE4A", "I102", "M. A",
            semaine1.plusDays(4), LocalTime.of(14, 0), LocalTime.of(16, 0), EventType.EXAM),

    )

    fun getEventsForDate(date: LocalDate): List<Event> =
        events.filter { it.date == date }.sortedBy { it.startTime }

    fun generateScheduleText(weekStart: LocalDate): String {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val sb = StringBuilder()
        for (i in 0..4) {
            val date = weekStart.plusDays(i.toLong())
            val dayEvents = getEventsForDate(date)
            if (dayEvents.isNotEmpty()) {
                sb.appendLine("${dayName(i)} ${date.dayOfMonth} ${monthName(date.monthValue)} ${date.year}:")
                dayEvents.forEach { e ->
                    sb.appendLine("  • ${e.startTime.format(fmt)}-${e.endTime.format(fmt)}: ${e.title} – ${e.location} (${e.instructor}) [${e.type.label}]")
                }
                sb.appendLine()
            }
        }
        return if (sb.isEmpty()) "Aucun cours cette semaine." else sb.toString()
    }

    private fun dayName(i: Int) = arrayOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi")[i]

    private fun monthName(m: Int) = arrayOf(
        "", "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )[m]
}
