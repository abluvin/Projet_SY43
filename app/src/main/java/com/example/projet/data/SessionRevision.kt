package com.example.projet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

enum class CampusType(val label: String) {
    SEVENANS("SEVENANS"),
    BELFORT("BELFORT"),
    MONTBELLIARD("MONTBELLIARD"),
    AUTRE("AUTRE")
}

@Entity(tableName = "collaborations")
data class Collaboration(
    @PrimaryKey
    val id: String,
    val name: String,
    val ue: String,
    val description: String,
    val campus: CampusType,
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
