package com.example.projet.data.repository

import com.example.projet.data.Collaboration
import com.example.projet.data.dao.CollaborationDao
import kotlinx.coroutines.flow.Flow

class CollaborationRepository(private val dao: CollaborationDao) {

    fun getAll(): Flow<List<Collaboration>> = dao.getAll()

    suspend fun count(): Int = dao.count()

    suspend fun insert(collaboration: Collaboration) = dao.insert(collaboration)

    suspend fun update(collaboration: Collaboration) = dao.update(collaboration)

    suspend fun delete(collaboration: Collaboration) = dao.delete(collaboration)
}
