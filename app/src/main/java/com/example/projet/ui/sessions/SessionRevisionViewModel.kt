package com.example.projet.ui.sessions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.CampusType
import com.example.projet.data.Collaboration
import com.example.projet.data.repository.CollaborationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class CollaborationViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CollaborationRepository(
        (application as ProjetApplication).database.collaborationDao()
    )
    private val firestoreRepo = com.example.projet.data.firebase.CollaborationFireStoreRepository(com.example.projet.data.firebase.FireStoreRepository())

    val collaborations: StateFlow<List<Collaboration>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filterCampus = MutableStateFlow<CampusType?>(null)
    val filterCampus: StateFlow<CampusType?> = _filterCampus.asStateFlow()

    init {
        viewModelScope.launch {
            if (repo.count() == 0) {
                sampleCollaborations().forEach { repo.insert(it) }
            }
        }
    }

    fun setFilterCampus(campus: CampusType?) {
        _filterCampus.value = campus
    }

    fun createCollaboration(collaboration: Collaboration) {
        viewModelScope.launch {
            repo.insert(collaboration)
            try { firestoreRepo.saveCollaboration(collaboration) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun joinCollaboration(collaborationId: String, userId: String, userName: String) {
        viewModelScope.launch {
            val current = collaborations.value.find { it.id == collaborationId } ?: return@launch
            if (current.participantIds.contains(userId)) return@launch
            if (current.participantIds.size >= current.maxParticipants) return@launch
            val newIds = current.participantIds + userId
            val updated = current.copy(
                participantIds = newIds,
                participantNames = current.participantNames + userName,
                status = if (newIds.size >= current.maxParticipants) "FULL" else "OPEN"
            )
            repo.update(updated)
            try { firestoreRepo.saveCollaboration(updated) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun leaveCollaboration(collaborationId: String, userId: String) {
        viewModelScope.launch {
            val current = collaborations.value.find { it.id == collaborationId } ?: return@launch
            if (!current.participantIds.contains(userId)) return@launch
            val idx = current.participantIds.indexOf(userId)
            val updated = current.copy(
                participantIds = current.participantIds.toMutableList().also { it.removeAt(idx) },
                participantNames = current.participantNames.toMutableList().also { it.removeAt(idx) },
                status = "OPEN"
            )
            repo.update(updated)
            try { firestoreRepo.saveCollaboration(updated) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun sampleCollaborations() = listOf(
        Collaboration(
            id = UUID.randomUUID().toString(),
            name = "Révision SY43 – Modélisation UML",
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
        Collaboration(
            id = UUID.randomUUID().toString(),
            name = "TD MA50 – Analyse numérique",
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
        Collaboration(
            id = UUID.randomUUID().toString(),
            name = "TP LO43 – Prolog",
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
        Collaboration(
            id = UUID.randomUUID().toString(),
            name = "Révision HM40 – Histoire des sciences",
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
