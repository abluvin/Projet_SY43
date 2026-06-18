package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Poll
import kotlinx.coroutines.flow.Flow

@Dao
interface PollDao {
    @Query("SELECT * FROM poll WHERE postId = :postId ORDER BY timestamp DESC")
    fun getByPost(postId: Int): Flow<List<Poll>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poll: Poll): Long

    @Delete
    suspend fun delete(poll: Poll)
}
