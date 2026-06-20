package com.example.projet.data.firebase

import com.example.projet.data.ChatItem
import com.example.projet.data.Message

class ChatFireStoreRepository(
    private val firestore: FireStoreRepository
) {
    private fun getConversationsPath(userId: Int) = "users/$userId/conversations"
    private fun getMessagesPath(userId: Int, chatId: Int) = "users/$userId/conversations/$chatId/messages"

    suspend fun createConversation(userId: Int, chatItem: ChatItem) {
        firestore.add(getConversationsPath(userId), chatItem.id.toString(), chatItem)
    }

    suspend fun sendMessage(userId: Int, message: Message) {
        // Enregistrer le message dans la conversation spécifique de l'utilisateur
        firestore.add(getMessagesPath(userId, message.chatItemId), message.id.toString(), message)
        
        // Mettre à jour le dernier message dans l'objet conversation
        val convPath = getConversationsPath(userId)
        val chatItem = firestore.getById(convPath, message.chatItemId.toString(), ChatItem::class.java)
        chatItem?.let {
            val updatedChat = it.copy(lastMessage = message.text, time = message.time)
            firestore.add(convPath, updatedChat.id.toString(), updatedChat)
        }
    }

    suspend fun getAllConversations(userId: Int): List<ChatItem> {
        return firestore.getAll(getConversationsPath(userId), ChatItem::class.java)
    }
    
    suspend fun getMessages(userId: Int, chatId: Int): List<Message> {
        return firestore.getAll(getMessagesPath(userId, chatId), Message::class.java)
    }
}
