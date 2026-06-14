package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.PollOption
import kotlinx.coroutines.flow.Flow

@Dao
interface PollOptionDao {

    @Query("SELECT * FROM poll_option WHERE pollId = :pollId ORDER BY id")
    fun getByPoll(pollId: Int): Flow<List<PollOption>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pollOption: PollOption): Long

    @Update
    suspend fun update(pollOption: PollOption)

    @Delete
    suspend fun delete(pollOption: PollOption)

    @Query("DELETE FROM poll_option WHERE pollId = :pollId")
    suspend fun deleteByPoll(pollId: Int)
}

