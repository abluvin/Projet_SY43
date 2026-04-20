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

    private val eventTypeMap = mapOf(
        "CM" to EventType.COURS,
        "TD" to EventType.TD,
        "TP" to EventType.TP,
        "EXAM" to EventType.EXAM
    )

    fun parseScheduleText(text: String): List<Event> {
        val events = mutableListOf<Event>()
        val lines = text.trim().split("\n")
        
        // Regex plus flexible pour gérer les espaces/tabs variables
        val regex = Regex(
            """^([A-Z0-9]{2,4}(?:\s*\|\s*[A-Z0-9]{2,4})?)\s+([A-Z]{2,4})(\d?)\s+([a-zé]+)\s+(\d{1,2}):(\d{2})\s+(\d{1,2}):(\d{2})\s+(\d)\s+([A-Za-zé]+)\s+(.+)$""",
            RegexOption.IGNORE_CASE
        )
        
        var eventId = 1000
        val baseWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            // Remplacer les tabs/espaces multiples par un seul espace
            val normalizedLine = trimmedLine.replace(Regex("""\s+"""), " ")
            
            val matchResult = regex.find(normalizedLine)
            if (matchResult != null) {
                try {
                    val groups = matchResult.groupValues
                    val courseCode = groups[1]
                    val courseType = groups[2]
                    val groupNumber = groups[3]
                    val dayName = groups[4]
                    val startHour = groups[5]
                    val startMinute = groups[6]
                    val endHour = groups[7]
                    val endMinute = groups[8]
                    val frequency = groups[9]
                    val mode = groups[10]
                    val rooms = groups[11]

                    val dayOffsetInWeek = dayMap[dayName.lowercase()] ?: continue
                    val eventDate = baseWeekMonday.plusDays(dayOffsetInWeek.toLong())
                    
                    val eventTypeObj = when {
                        courseType.contains("CM", ignoreCase = true) -> EventType.COURS
                        courseType.contains("TD", ignoreCase = true) -> EventType.TD
                        courseType.contains("TP", ignoreCase = true) -> EventType.TP
                        else -> EventType.COURS
                    }
                    
                    val event = Event(
                        id = (eventId++).toString(),
                        code = courseCode.replace("|", " |"),
                        title = "$courseCode - $courseType${if (groupNumber.isNotEmpty()) " $groupNumber" else ""}",
                        location = rooms.trim(),
                        instructor = "Importé",
                        date = eventDate,
                        startTime = LocalTime.of(startHour.toInt(), startMinute.toInt()),
                        endTime = LocalTime.of(endHour.toInt(), endMinute.toInt()),
                        type = eventTypeObj
                    )
                    
                    events.add(event)
                } catch (e: Exception) {
                    // Ignorer les lignes qui ne peuvent pas être parsées
                    continue
                }
            }
        }
        
        return events
    }
}
