package com.example.projet.data.repository

import com.example.projet.data.CourseMaterial
import com.example.projet.data.dao.CourseMaterialDao
import kotlinx.coroutines.flow.Flow

class CourseMaterialRepository(private val dao: CourseMaterialDao) {

    fun getAll(): Flow<List<CourseMaterial>> = dao.getAll()

    suspend fun insert(material: CourseMaterial): Long = dao.insert(material)

    suspend fun delete(material: CourseMaterial) = dao.delete(material)
}
