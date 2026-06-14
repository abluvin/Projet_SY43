package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.PollVote
import kotlinx.coroutines.flow.Flow

@Dao
interface PollVoteDao {

    @Query("SELECT * FROM poll_vote WHERE optionId = :optionId ORDER BY timestamp DESC")
    fun getByOption(optionId: Int): Flow<List<PollVote>>

    @Query("SELECT * FROM poll_vote WHERE userId = :userId ORDER BY timestamp DESC")
    fun getByUser(userId: Int): Flow<List<PollVote>>

    @Query("SELECT * FROM poll_vote WHERE optionId = :optionId AND userId = :userId LIMIT 1")
    suspend fun getUserVote(optionId: Int, userId: Int): PollVote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pollVote: PollVote): Long

    @Delete
    suspend fun delete(pollVote: PollVote)

    @Query("DELETE FROM poll_vote WHERE optionId IN (SELECT id FROM poll_option WHERE pollId = :pollId)")
    suspend fun deleteVotesByPoll(pollId: Int)
}

