package com.example.projet.data.repository

import com.example.projet.data.ChatItem
import com.example.projet.data.Message
import com.example.projet.data.dao.ChatItemDao
import com.example.projet.data.dao.MessageDao
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatItemDao: ChatItemDao,
    private val messageDao: MessageDao
) {

    fun getAllConversations(): Flow<List<ChatItem>> = chatItemDao.getAll()

    fun getChatItem(id: Int): Flow<ChatItem?> = chatItemDao.getById(id)

    suspend fun insertConversation(chatItem: ChatItem): Long = chatItemDao.insert(chatItem)

    suspend fun updateConversation(chatItem: ChatItem) = chatItemDao.update(chatItem)

    suspend fun deleteConversation(chatItem: ChatItem) = chatItemDao.delete(chatItem)

    fun getMessages(chatItemId: Int): Flow<List<Message>> = messageDao.getByChatItem(chatItemId)

    fun getLastMessage(chatItemId: Int): Flow<String?> = messageDao.getLastMessage(chatItemId)

    suspend fun getMessageById(id: Int): Message? = messageDao.getById(id)

    suspend fun insertMessage(message: Message) = messageDao.insert(message)

    suspend fun updateMessage(message: Message) = messageDao.update(message)

    suspend fun deleteMessage(message: Message) = messageDao.delete(message)

    suspend fun deleteAllMessages(chatItemId: Int) = messageDao.deleteByChatItem(chatItemId)

    suspend fun markAsRead(id: Int) = chatItemDao.markAsRead(id)

    suspend fun getConversationByName(name: String): ChatItem? = chatItemDao.getByName(name)

    suspend fun updateLastMessage(id: Int, lastMessage: String, time: String) = chatItemDao.updateLastMessage(id, lastMessage, time)
}
