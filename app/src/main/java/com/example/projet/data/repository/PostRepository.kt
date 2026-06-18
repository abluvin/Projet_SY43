package com.example.projet.data.repository

import com.example.projet.data.*
import com.example.projet.data.dao.*
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val voiceMessageDao: VoiceMessageDao,
    private val pollDao: PollDao,
    private val pollOptionDao: PollOptionDao,
    private val pollVoteDao: PollVoteDao
) {
    fun getAll(): Flow<List<Post>> = postDao.getAll()
    fun getByUser(userId: Int): Flow<List<Post>> = postDao.getByUser(userId)
    suspend fun insert(post: Post): Long = postDao.insert(post)
    suspend fun delete(post: Post) = postDao.delete(post)

    fun getComments(postId: Int): Flow<List<Comment>> = commentDao.getByPost(postId)
    suspend fun insertComment(comment: Comment): Long = commentDao.insert(comment)
    suspend fun deleteComment(comment: Comment) = commentDao.delete(comment)

    fun getVoiceMessages(postId: Int): Flow<List<VoiceMessage>> = voiceMessageDao.getByPost(postId)
    suspend fun insertVoiceMessage(voiceMessage: VoiceMessage): Long = voiceMessageDao.insert(voiceMessage)
    suspend fun deleteVoiceMessage(voiceMessage: VoiceMessage) = voiceMessageDao.delete(voiceMessage)

    fun getPolls(postId: Int): Flow<List<Poll>> = pollDao.getByPost(postId)
    suspend fun insertPoll(poll: Poll): Long = pollDao.insert(poll)
    suspend fun deletePoll(poll: Poll) = pollDao.delete(poll)

    fun getPollOptions(pollId: Int): Flow<List<PollOption>> = pollOptionDao.getByPoll(pollId)
    suspend fun insertPollOption(pollOption: PollOption): Long = pollOptionDao.insert(pollOption)
    suspend fun updatePollOption(pollOption: PollOption) = pollOptionDao.update(pollOption)
    suspend fun deletePollOption(pollOption: PollOption) = pollOptionDao.delete(pollOption)

    fun getPollVotes(optionId: Int): Flow<List<PollVote>> = pollVoteDao.getByOption(optionId)
    suspend fun getUserVote(optionId: Int, userId: Int): PollVote? = pollVoteDao.getUserVote(optionId, userId)
    suspend fun insertPollVote(pollVote: PollVote): Long = pollVoteDao.insert(pollVote)
    suspend fun deletePollVote(pollVote: PollVote) = pollVoteDao.delete(pollVote)
}
