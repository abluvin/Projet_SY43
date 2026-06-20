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

    suspend fun insertMessage(message: Message): Long = messageDao.insert(message)

    suspend fun deleteMessage(message: Message) = messageDao.delete(message)

    suspend fun deleteAllMessages(chatItemId: Int) = messageDao.deleteByChatItem(chatItemId)
}
