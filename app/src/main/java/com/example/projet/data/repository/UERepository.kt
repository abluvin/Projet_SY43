package com.example.projet.data.repository

import com.example.projet.data.UE
import com.example.projet.data.UserUE
import com.example.projet.data.UserUEWithDetails
import com.example.projet.data.dao.UEDao
import com.example.projet.data.dao.UserUEDao
import kotlinx.coroutines.flow.Flow

class UERepository(private val ueDao: UEDao, private val userUEDao: UserUEDao) {

    fun getAllUEs(): Flow<List<UE>> = ueDao.getAll()

    fun getUserUEs(userId: Int): Flow<List<UserUEWithDetails>> =
        userUEDao.getByUserWithDetails(userId)

    suspend fun addUserUE(userId: Int, ueId: Int, enseigne: Boolean): Boolean {
        if (userUEDao.countByUser(userId) >= 10) return false
        userUEDao.insert(UserUE(userId = userId, ueId = ueId, enseigne = enseigne))
        return true
    }

    suspend fun removeUserUE(userId: Int, ueId: Int) =
        userUEDao.deleteByUserAndUe(userId, ueId)

    suspend fun updateEnseigne(userId: Int, ueId: Int, enseigne: Boolean) =
        userUEDao.updateEnseigne(userId, ueId, enseigne)
}
