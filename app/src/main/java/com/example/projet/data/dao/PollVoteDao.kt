package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.PollVote
import kotlinx.coroutines.flow.Flow

@Dao
interface PollVoteDao {
    @Query("SELECT * FROM poll_vote WHERE optionId = :optionId ORDER BY timestamp DESC")
    fun getByOption(optionId: Int): Flow<List<PollVote>>

    @Query("SELECT * FROM poll_vote WHERE optionId = :optionId AND userId = :userId LIMIT 1")
    suspend fun getUserVote(optionId: Int, userId: Int): PollVote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pollVote: PollVote): Long

    @Delete
    suspend fun delete(pollVote: PollVote)
}
