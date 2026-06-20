package com.example.projet.data.firebase

import com.example.projet.data.ChatItem
import com.example.projet.data.Message

class ChatFireStoreRepository(
    private val firestore: FireStoreRepository
) {
    private val convCollection = "conversations"
    private val msgCollection = "messages"

    suspend fun createConversation(chatItem: ChatItem) {
        firestore.add(convCollection, chatItem.id.toString(), chatItem)
    }

    suspend fun sendMessage(message: Message) {
        firestore.add(msgCollection, message.id.toString(), message)
    }

    suspend fun getAllConversations(): List<ChatItem> {
        return firestore.getAll(convCollection, ChatItem::class.java)
    }
}
