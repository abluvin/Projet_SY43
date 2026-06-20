package com.example.projet.data.firebase

import com.example.projet.data.Event
import com.example.projet.data.EventType
import java.time.LocalDate
import java.time.LocalTime

class EventFireStoreRepository(
    private val firestore: FireStoreRepository
) {
    private fun getPath(userId: Int) = "users/$userId/events"

    suspend fun saveEvent(userId: Int, event: Event) {
        val data = mapOf(
            "id" to event.id,
            "title" to event.title,
            "code" to event.code,
            "location" to event.location,
            "instructor" to event.instructor,
            "date" to event.date.toString(),
            "startTime" to event.startTime.toString(),
            "endTime" to event.endTime.toString(),
            "type" to event.type.name,
            "isGlobal" to event.isGlobal,
            "targetUECodes" to event.targetUECodes,
            "targetBranches" to event.targetBranches
        )
        firestore.add(getPath(userId), event.id, data)
    }

    suspend fun getEvents(userId: Int): List<Event> {
        val list = firestore.getAll(getPath(userId), Map::class.java)
        return list.map { map ->
            Event(
                id = map["id"] as String,
                title = map["title"] as String,
                code = map["code"] as String,
                location = map["location"] as String,
                instructor = map["instructor"] as String,
                date = LocalDate.parse(map["date"] as String),
                startTime = LocalTime.parse(map["startTime"] as String),
                endTime = LocalTime.parse(map["endTime"] as String),
                type = EventType.valueOf(map["type"] as String),
                isGlobal = map["isGlobal"] as Boolean,
                targetUECodes = (map["targetUECodes"] as? List<String>) ?: emptyList(),
                targetBranches = (map["targetBranches"] as? List<String>) ?: emptyList()
            )
        }
    }
}
