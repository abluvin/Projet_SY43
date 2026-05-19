package com.example.projet.data
import java.time.LocalDate
import java.time.LocalTime


enum class CampusType(val label: String) {
    SEVENANS("SEVENANS"),
    BELFORT("BELFORT"),
    MONTBELLIARD("MONTBELLIARD"),
    AUTRE("AUTRE")
}

data class SessionRevision (
    val id: String,
    val seance_name: String,
    val ue : String,
    val description : String,
    val campus : CampusType,
    val room: String,
    val creatorId: String = "",
    val creatorName: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: List<String> = emptyList(),
    val status: String = "OPEN",
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val maxParticipants: Int = 10,
)