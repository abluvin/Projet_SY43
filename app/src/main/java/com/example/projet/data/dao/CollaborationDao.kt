package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Collaboration
import kotlinx.coroutines.flow.Flow

@Dao
interface CollaborationDao {

    @Query("SELECT * FROM collaborations ORDER BY date ASC, startTime ASC")
    fun getAll(): Flow<List<Collaboration>>

    @Query("SELECT COUNT(*) FROM collaborations")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collaboration: Collaboration)

    @Update
    suspend fun update(collaboration: Collaboration)

    @Delete
    suspend fun delete(collaboration: Collaboration)

    @Query("DELETE FROM collaborations")
    suspend fun deleteAll()
}
