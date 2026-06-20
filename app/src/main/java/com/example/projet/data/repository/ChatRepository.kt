package com.example.projet.data.repository

import com.example.projet.data.ChatItem
import com.example.projet.data.ChatMember
import com.example.projet.data.ChatPoll
import com.example.projet.data.ChatPollOption
import com.example.projet.data.ChatPollVote
import com.example.projet.data.Message
import com.example.projet.data.dao.ChatItemDao
import com.example.projet.data.dao.ChatMemberDao
import com.example.projet.data.dao.ChatPollDao
import com.example.projet.data.dao.ChatPollOptionDao
import com.example.projet.data.dao.ChatPollVoteDao
import com.example.projet.data.dao.MessageDao
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatItemDao: ChatItemDao,
    private val messageDao: MessageDao,
    private val chatPollDao: ChatPollDao,
    private val chatPollOptionDao: ChatPollOptionDao,
    private val chatPollVoteDao: ChatPollVoteDao,
    private val chatMemberDao: ChatMemberDao
) {

    fun getAllConversations(): Flow<List<ChatItem>> = chatItemDao.getAll()

    fun getConversationsForUser(userId: Int): Flow<List<ChatItem>> = chatItemDao.getForUser(userId)

    suspend fun addMember(chatItemId: Int, userId: Int) =
        chatMemberDao.insert(ChatMember(chatItemId = chatItemId, userId = userId))

    fun getOtherMemberName(chatItemId: Int, currentUserId: Int): Flow<String?> =
        chatMemberDao.getOtherMemberName(chatItemId, currentUserId)

    fun getChatItem(id: Int): Flow<ChatItem?> = chatItemDao.getById(id)

    suspend fun insertConversation(chatItem: ChatItem): Long = chatItemDao.insert(chatItem)

    suspend fun updateConversation(chatItem: ChatItem) = chatItemDao.update(chatItem)

    suspend fun deleteConversation(chatItem: ChatItem) = chatItemDao.delete(chatItem)

    fun getMessages(chatItemId: Int): Flow<List<Message>> = messageDao.getByChatItem(chatItemId)

    fun getLastMessage(chatItemId: Int): Flow<String?> = messageDao.getLastMessage(chatItemId)

    suspend fun insertMessage(message: Message): Long = messageDao.insert(message)

    suspend fun deleteMessage(message: Message) = messageDao.delete(message)

    suspend fun deleteAllMessages(chatItemId: Int) = messageDao.deleteByChatItem(chatItemId)

    // Poll methods
    fun getPoll(pollId: Int): Flow<ChatPoll?> = chatPollDao.getById(pollId)

    fun getPollOptions(pollId: Int): Flow<List<ChatPollOption>> = chatPollOptionDao.getByPoll(pollId)

    fun getUserVotesForPoll(userId: Int, pollId: Int): Flow<List<ChatPollVote>> =
        chatPollVoteDao.getUserVotesForPoll(userId, pollId)

    suspend fun insertPoll(poll: ChatPoll): Long = chatPollDao.insert(poll)

    suspend fun insertPollOption(option: ChatPollOption): Long = chatPollOptionDao.insert(option)

    suspend fun addVote(optionId: Int, userId: Int) {
        val existing = chatPollVoteDao.getVote(optionId, userId)
        if (existing == null) {
            chatPollVoteDao.insert(ChatPollVote(optionId = optionId, userId = userId))
            chatPollOptionDao.incrementVote(optionId)
        } else {
            chatPollVoteDao.delete(existing)
            chatPollOptionDao.decrementVote(optionId)
        }
    }
}
