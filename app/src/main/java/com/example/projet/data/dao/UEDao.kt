package com.example.projet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projet.data.UE
import kotlinx.coroutines.flow.Flow

@Dao
interface UEDao {
    @Query("SELECT * FROM ue ORDER BY code ASC")
    fun getAll(): Flow<List<UE>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ues: List<UE>)
}
