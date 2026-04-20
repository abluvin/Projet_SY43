package com.example.projet.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Parser pour transformer le texte OCR/copié en événements d'emploi du temps
 * Format attendu: CODE    TYPE    GROUPE    JOUR    HEURE_DEBUT    HEURE_FIN    FREQUENCE    MODE    SALLES
 */
object ScheduleParser {

    private val dayMap = mapOf(
        "lundi" to 0,
        "mardi" to 1,
        "mercredi" to 2,
        "jeudi" to 3,
        "vendredi" to 4
    )

    fun parseScheduleText(text: String, numberOfWeeks: Int = 4): List<Event> {
        val events = mutableListOf<Event>()
        val lines = text.trim().split("\n")
        
        var eventId = 1000
        val baseWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            // Diviser par espaces/tabs multiples
            val parts = trimmedLine.split(Regex("""\s+""")).filter { it.isNotEmpty() }

            if (parts.size < 8) continue // Pas assez de colonnes

            try {
                // Trouver le type de cours (CM, TD, TP)
                var typeIndex = -1
                var courseType = ""
                for (i in 1 until minOf(3, parts.size)) {
                    if (parts[i].matches(Regex("^(CM|TD|TP|EXAM)\\d?[A-Z]?$", RegexOption.IGNORE_CASE))) {
                        typeIndex = i
                        courseType = parts[i]
                        break
                    }
                }

                if (typeIndex == -1) continue

                val courseCode = parts[0]

                // Le groupe peut être immédiatement après le type ou être un élément séparé
                var groupNumber = ""
                var dayIndex = typeIndex + 1

                // Vérifier si le groupe est dans le type (ex: TP1, CM1) ou séparé (ex: TP A)
                if (courseType.length > 2) {
                    // Type contient le groupe (ex: TP1, CMA)
                    groupNumber = courseType.substring(2)
                    courseType = courseType.substring(0, 2)
                } else if (dayIndex < parts.size && parts[dayIndex].matches(Regex("^[A-Z0-9]$"))) {
                    // Groupe séparé
                    groupNumber = parts[dayIndex]
                    dayIndex++
                }

                if (dayIndex >= parts.size) continue

                // Trouver le jour
                val dayName = parts[dayIndex].lowercase()
                if (!dayMap.containsKey(dayName)) continue

                val dayOffsetInWeek = dayMap[dayName]!!

                // Heures et minutes
                if (dayIndex + 4 >= parts.size) continue

                val timeParts = parts[dayIndex + 1].split(":")
                if (timeParts.size != 2) continue
                val startHour = timeParts[0].toInt()
                val startMinute = timeParts[1].toInt()

                val endTimeParts = parts[dayIndex + 2].split(":")
                if (endTimeParts.size != 2) continue
                val endHour = endTimeParts[0].toInt()
                val endMinute = endTimeParts[1].toInt()

                val frequency = parts[dayIndex + 3]
                val mode = parts[dayIndex + 4]

                // Le reste sont les salles
                val rooms = parts.drop(dayIndex + 5).joinToString(" ")

                // Déterminer le type d'événement
                val eventTypeObj = when {
                    courseType.contains("CM", ignoreCase = true) -> EventType.COURS
                    courseType.contains("TD", ignoreCase = true) -> EventType.TD
                    courseType.contains("TP", ignoreCase = true) -> EventType.TP
                    else -> EventType.COURS
                }

                // Créer l'événement pour chaque semaine
                for (weekOffset in 0 until numberOfWeeks) {
                    val eventDate = baseWeekMonday.plusDays(dayOffsetInWeek.toLong()).plusWeeks(weekOffset.toLong())

                    val event = Event(
                        id = (eventId++).toString(),
                        code = courseCode,
                        title = "$courseCode - $courseType${if (groupNumber.isNotEmpty()) " $groupNumber" else ""}",
                        location = rooms.trim(),
                        instructor = "Importé",
                        date = eventDate,
                        startTime = LocalTime.of(startHour, startMinute),
                        endTime = LocalTime.of(endHour, endMinute),
                        type = eventTypeObj
                    )

                    events.add(event)
                }
            } catch (e: Exception) {
                // Ignorer les lignes qui ne peuvent pas être parsées
                continue
            }
        }
        
        return events
    }
}
