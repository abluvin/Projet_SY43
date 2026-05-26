package com.example.projet.data.repository

import com.example.projet.data.Event
import com.example.projet.data.dao.EventDao
import kotlinx.coroutines.flow.Flow

class EventRepository(private val dao: EventDao) {

    fun getAll(): Flow<List<Event>> = dao.getAll()

    fun getByDate(date: String): Flow<List<Event>> = dao.getByDate(date)

    suspend fun insert(event: Event) = dao.insert(event)

    suspend fun insertAll(events: List<Event>) = dao.insertAll(events)

    suspend fun delete(event: Event) = dao.delete(event)

    suspend fun deleteAll() = dao.deleteAll()
}
