package com.example.projet.data.firebase

import com.example.projet.data.CourseMaterial

class CourseMaterialFireStoreRepository(
    private val firestore: FireStoreRepository
) {
    private fun getPath(userId: Int) = "users/$userId/course_materials"

    suspend fun saveMaterial(userId: Int, material: CourseMaterial) {
        firestore.add(getPath(userId), material.id.toString(), material)
    }

    suspend fun getMaterials(userId: Int): List<CourseMaterial> {
        return firestore.getAll(getPath(userId), CourseMaterial::class.java)
    }
}
