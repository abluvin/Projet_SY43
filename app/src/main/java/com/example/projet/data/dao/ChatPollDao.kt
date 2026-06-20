package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.ChatPoll
import com.example.projet.data.ChatPollOption
import com.example.projet.data.ChatPollVote
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatPollDao {
    @Query("SELECT * FROM chat_poll WHERE id = :id")
    fun getById(id: Int): Flow<ChatPoll?>

    @Insert
    suspend fun insert(poll: ChatPoll): Long
}

@Dao
interface ChatPollOptionDao {
    @Query("SELECT * FROM chat_poll_option WHERE pollId = :pollId ORDER BY id ASC")
    fun getByPoll(pollId: Int): Flow<List<ChatPollOption>>

    @Insert
    suspend fun insert(option: ChatPollOption): Long

    @Query("UPDATE chat_poll_option SET voteCount = voteCount + 1 WHERE id = :optionId")
    suspend fun incrementVote(optionId: Int)

    @Query("UPDATE chat_poll_option SET voteCount = voteCount - 1 WHERE id = :optionId")
    suspend fun decrementVote(optionId: Int)
}

@Dao
interface ChatPollVoteDao {
    @Query("SELECT * FROM chat_poll_vote WHERE optionId = :optionId AND userId = :userId")
    suspend fun getVote(optionId: Int, userId: Int): ChatPollVote?

    @Query("SELECT * FROM chat_poll_vote WHERE userId = :userId AND optionId IN (SELECT id FROM chat_poll_option WHERE pollId = :pollId)")
    fun getUserVotesForPoll(userId: Int, pollId: Int): Flow<List<ChatPollVote>>

    @Insert
    suspend fun insert(vote: ChatPollVote)

    @Delete
    suspend fun delete(vote: ChatPollVote)
}
