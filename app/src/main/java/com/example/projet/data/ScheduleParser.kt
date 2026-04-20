package com.example.projet.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

object ScheduleParser {

    private val dayMap = mapOf(
        "lundi" to DayOfWeek.MONDAY,
        "mardi" to DayOfWeek.TUESDAY,
        "mercredi" to DayOfWeek.WEDNESDAY,
        "jeudi" to DayOfWeek.THURSDAY,
        "vendredi" to DayOfWeek.FRIDAY
    )

    private val typeRegex = Regex("^(CM|TD|TP|EXAM)\\d*$", RegexOption.IGNORE_CASE)

    private fun getSemesterRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val month = today.monthValue
        val year = today.year
        return when {
            month in 3..8 -> Pair(LocalDate.of(year, 3, 1), LocalDate.of(year, 6, 30))
            month in 9..12 -> Pair(LocalDate.of(year, 9, 1), LocalDate.of(year + 1, 1, 31))
            else -> Pair(LocalDate.of(year - 1, 9, 1), LocalDate.of(year, 1, 31)) // jan/fev = fin automne
        }
    }

    fun parseScheduleText(text: String): List<Event> {
        val events = mutableListOf<Event>()
        val lines = text.trim().split("\n")
        var eventId = 1000

        val (semesterStart, semesterEnd) = getSemesterRange()
        val firstMonday = semesterStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val parts = trimmed.split(Regex("""\s+""")).filter { it.isNotEmpty() }
            if (parts.size < 7) continue

            try {
                var idx = 0

                // Collect course codes, skipping "|" separators (e.g. "WE4A | WE4B")
                val codes = mutableListOf<String>()
                while (idx < parts.size && !typeRegex.matches(parts[idx])) {
                    if (parts[idx] != "|") codes.add(parts[idx])
                    idx++
                }
                if (codes.isEmpty() || idx >= parts.size) continue

                val courseCode = codes.joinToString(" | ")
                val primaryCode = codes.first()

                // Full type token e.g. "CM1", "TD2", "TP1"
                val fullType = parts[idx++]
                val typePrefix = fullType.takeWhile { it.isLetter() }.uppercase()
                val sessionNum = fullType.dropWhile { it.isLetter() }

                // Optional group letter (single uppercase letter A-Z)
                var groupLetter = ""
                if (idx < parts.size && parts[idx].matches(Regex("^[A-Z]$"))) {
                    groupLetter = parts[idx++]
                }

                // Day name
                if (idx >= parts.size) continue
                val dayOfWeek = dayMap[parts[idx++].lowercase()] ?: continue

                // Start / end time
                if (idx + 1 >= parts.size) continue
                val startTime = LocalTime.parse(parts[idx++])
                val endTime = LocalTime.parse(parts[idx++])

                // Frequency (1 = hebdo, 2 = bi-hebdo)
                if (idx >= parts.size) continue
                val frequency = parts[idx++].toIntOrNull() ?: 1

                // Skip mode column ("Présentiel" etc.)
                idx++

                // Everything remaining = rooms
                val rooms = parts.drop(idx).joinToString(" ")

                val eventType = when (typePrefix) {
                    "CM" -> EventType.COURS
                    "TD" -> EventType.TD
                    "TP" -> EventType.TP
                    "EXAM" -> EventType.EXAM
                    else -> EventType.COURS
                }

                val title = "$courseCode - $typePrefix$sessionNum${if (groupLetter.isNotEmpty()) " $groupLetter" else ""}"

                // Generate one event per applicable week across the semester
                var weekMonday = firstMonday
                var weekNum = 1

                while (!weekMonday.isAfter(semesterEnd)) {
                    val include = when {
                        frequency != 2 -> true
                        groupLetter == "A" -> weekNum % 2 == 1  // semaines impaires
                        groupLetter == "B" -> weekNum % 2 == 0  // semaines paires
                        else -> weekNum % 2 == 1
                    }

                    if (include) {
                        val eventDate = weekMonday.with(TemporalAdjusters.nextOrSame(dayOfWeek))
                        if (!eventDate.isAfter(semesterEnd)) {
                            events.add(
                                Event(
                                    id = (eventId++).toString(),
                                    code = primaryCode,
                                    title = title,
                                    location = rooms,
                                    instructor = "Importé",
                                    date = eventDate,
                                    startTime = startTime,
                                    endTime = endTime,
                                    type = eventType
                                )
                            )
                        }
                    }

                    weekMonday = weekMonday.plusWeeks(1)
                    weekNum++
                }

            } catch (e: Exception) {
                continue
            }
        }

        return events
    }
}
