package com.example.projet.ui.sessions

import androidx.lifecycle.ViewModel
import com.example.projet.data.CampusType
import com.example.projet.data.SessionRevision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime

class SessionRevisionViewModel : ViewModel() {

    private val _sessions = MutableStateFlow(sampleSessions())
    val sessions: StateFlow<List<SessionRevision>> = _sessions.asStateFlow()

    private val _filterCampus = MutableStateFlow<CampusType?>(null)
    val filterCampus: StateFlow<CampusType?> = _filterCampus.asStateFlow()

    fun setFilterCampus(campus: CampusType?) {
        _filterCampus.value = campus
    }

    fun createSession(session: SessionRevision) {
        _sessions.value = _sessions.value + session
    }

    fun joinSession(sessionId: String, userId: String, userName: String) {
        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId &&
                session.participantIds.size < session.maxParticipants &&
                !session.participantIds.contains(userId)
            ) {
                val newIds = session.participantIds + userId
                session.copy(
                    participantIds = newIds,
                    participantNames = session.participantNames + userName,
                    status = if (newIds.size >= session.maxParticipants) "FULL" else "OPEN"
                )
            } else session
        }
    }

    fun leaveSession(sessionId: String, userId: String) {
        _sessions.value = _sessions.value.map { session ->
            if (session.id == sessionId && session.participantIds.contains(userId)) {
                val idx = session.participantIds.indexOf(userId)
                session.copy(
                    participantIds = session.participantIds.toMutableList().also { it.removeAt(idx) },
                    participantNames = session.participantNames.toMutableList().also { it.removeAt(idx) },
                    status = "OPEN"
                )
            } else session
        }
    }

    private fun sampleSessions() = listOf(
        SessionRevision(
            id = "1",
            seance_name = "Révision SY43 – Modélisation UML",
            ue = "SY43",
            description = "Révision des diagrammes de classes et de séquence avant le partiel.",
            campus = CampusType.SEVENANS,
            room = "M204",
            creatorId = "user_alice",
            creatorName = "Alice",
            participantIds = listOf("user_alice", "user_bob"),
            participantNames = listOf("Alice", "Bob"),
            status = "OPEN",
            date = LocalDate.now().plusDays(1),
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(16, 0),
            maxParticipants = 6
        ),
        SessionRevision(
            id = "2",
            seance_name = "TD MA50 – Analyse numérique",
            ue = "MA50",
            description = "Exercices sur les méthodes d'intégration numérique et équations différentielles.",
            campus = CampusType.SEVENANS,
            room = "L109",
            creatorId = "user_charlie",
            creatorName = "Charlie",
            participantIds = listOf("user_charlie"),
            participantNames = listOf("Charlie"),
            status = "OPEN",
            date = LocalDate.now().plusDays(2),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            maxParticipants = 8
        ),
        SessionRevision(
            id = "3",
            seance_name = "TP LO43 – Prolog",
            ue = "LO43",
            description = "Aide sur les TP Prolog, amenez votre ordinateur !",
            campus = CampusType.BELFORT,
            room = "S307",
            creatorId = "user_diana",
            creatorName = "Diana",
            participantIds = listOf("user_diana", "user_eve", "user_frank", "user_grace", "user_henry"),
            participantNames = listOf("Diana", "Eve", "Frank", "Grace", "Henry"),
            status = "FULL",
            date = LocalDate.now().plusDays(3),
            startTime = LocalTime.of(16, 0),
            endTime = LocalTime.of(18, 0),
            maxParticipants = 5
        ),
        SessionRevision(
            id = "4",
            seance_name = "Révision HM40 – Histoire des sciences",
            ue = "HM40",
            description = "Préparation à l'examen oral : grandes dates et inventions marquantes.",
            campus = CampusType.MONTBELLIARD,
            room = "A12",
            creatorId = "user_ivan",
            creatorName = "Ivan",
            participantIds = listOf("user_ivan", "user_julia"),
            participantNames = listOf("Ivan", "Julia"),
            status = "OPEN",
            date = LocalDate.now().plusDays(5),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(11, 0),
            maxParticipants = 10
        )
    )
}
