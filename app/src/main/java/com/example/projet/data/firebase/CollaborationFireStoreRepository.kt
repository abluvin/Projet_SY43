package com.example.projet.data.firebase

import com.example.projet.data.Collaboration
import com.example.projet.data.CampusType
import java.time.LocalDate
import java.time.LocalTime

class CollaborationFireStoreRepository(
    private val firestore: FireStoreRepository
) {
    private val collection = "collaborations"

    suspend fun saveCollaboration(collaboration: Collaboration) {
        val data = mapOf(
            "id" to collaboration.id,
            "name" to collaboration.name,
            "ue" to collaboration.ue,
            "description" to collaboration.description,
            "campus" to collaboration.campus.name,
            "room" to collaboration.room,
            "creatorId" to collaboration.creatorId,
            "creatorName" to collaboration.creatorName,
            "participantIds" to collaboration.participantIds,
            "participantNames" to collaboration.participantNames,
            "status" to collaboration.status,
            "date" to collaboration.date.toString(),
            "startTime" to collaboration.startTime.toString(),
            "endTime" to collaboration.endTime.toString(),
            "maxParticipants" to collaboration.maxParticipants
        )
        firestore.add(collection, collaboration.id, data)
    }

    suspend fun getCollaborations(): List<Collaboration> {
        val list = firestore.getAll(collection, Map::class.java)
        return list.map { map ->
            Collaboration(
                id = map["id"] as String,
                name = map["name"] as String,
                ue = map["ue"] as String,
                description = map["description"] as String,
                campus = CampusType.valueOf(map["campus"] as String),
                room = map["room"] as String,
                creatorId = map["creatorId"] as String,
                creatorName = map["creatorName"] as String,
                participantIds = (map["participantIds"] as? List<String>) ?: emptyList(),
                participantNames = (map["participantNames"] as? List<String>) ?: emptyList(),
                status = map["status"] as String,
                date = LocalDate.parse(map["date"] as String),
                startTime = LocalTime.parse(map["startTime"] as String),
                endTime = LocalTime.parse(map["endTime"] as String),
                maxParticipants = (map["maxParticipants"] as Long).toInt()
            )
        }
    }
}
