package com.example.projet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.projet.data.UserUE
import com.example.projet.data.UserUEWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface UserUEDao {
    @Transaction
    @Query("SELECT * FROM user_ue WHERE userId = :userId ORDER BY id ASC")
    fun getByUserWithDetails(userId: Int): Flow<List<UserUEWithDetails>>

    @Query("SELECT COUNT(*) FROM user_ue WHERE userId = :userId")
    suspend fun countByUser(userId: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userUE: UserUE)

    @Query("DELETE FROM user_ue WHERE userId = :userId AND ueId = :ueId")
    suspend fun deleteByUserAndUe(userId: Int, ueId: Int)

    @Query("UPDATE user_ue SET enseigne = :enseigne WHERE userId = :userId AND ueId = :ueId")
    suspend fun updateEnseigne(userId: Int, ueId: Int, enseigne: Boolean)
}
