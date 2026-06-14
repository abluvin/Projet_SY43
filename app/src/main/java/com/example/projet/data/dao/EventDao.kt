package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM event ORDER BY date, startTime")
    fun getAll(): Flow<List<Event>>

    @Query("SELECT * FROM event WHERE date = :date ORDER BY startTime")
    fun getByDate(date: String): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Delete
    suspend fun delete(event: Event)

    @Query("DELETE FROM event")
    suspend fun deleteAll()
}
