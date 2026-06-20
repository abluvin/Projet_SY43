package com.example.projet.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.ChatItem
import com.example.projet.data.Message
import com.example.projet.data.MessageType
import com.example.projet.data.firebase.ChatFireStoreRepository
import com.example.projet.data.firebase.FireStoreRepository
import com.example.projet.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: ChatRepository
    private val firestoreRepo = ChatFireStoreRepository(FireStoreRepository())
    private var currentUserId: Int = 0
    
    init {
        val db = (application as ProjetApplication).database
        repo = ChatRepository(db.chatItemDao(), db.messageDao())
        viewModelScope.launch {
            if (repo.getAllConversations().first().isEmpty()) seedConversations()
        }
    }

    fun setCurrentUserId(id: Int) {
        currentUserId = id
    }

    val conversations: StateFlow<List<ChatItem>> = repo.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMessages(chatItemId: Int): Flow<List<Message>> = repo.getMessages(chatItemId)

    fun getChatItem(id: Int): Flow<ChatItem?> = repo.getChatItem(id)

    fun getLastMessage(chatItemId: Int): Flow<String?> = repo.getLastMessage(chatItemId)

    fun sendMessage(chatItemId: Int, text: String) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val message = Message(chatItemId = chatItemId, text = text, isFromUser = true, time = time)
            
            // 1. Room
            val id = repo.insertMessage(message)
            val updatedMsg = message.copy(id = id.toInt())
            
            // 2. Firestore
            try {
                if (currentUserId != 0) {
                    firestoreRepo.sendMessage(currentUserId, updatedMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendAnnouncement(chatItemId: Int, text: String) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val message = Message(chatItemId = chatItemId, text = text, isFromUser = true, time = time, isAnnouncement = true)
            
            // 1. Room
            val id = repo.insertMessage(message)
            val updatedMsg = message.copy(id = id.toInt())
            
            // 2. Firestore
            try {
                if (currentUserId != 0) {
                    firestoreRepo.sendMessage(currentUserId, updatedMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createCourseHub(name: String, profName: String) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val chatItem = ChatItem(
                id = 0,
                name = name,
                lastMessage = "Hub créé par $profName",
                time = time,
                isCourse = true
            )
            
            // 1. Room
            val id = repo.insertConversation(chatItem)
            val updatedChatItem = chatItem.copy(id = id.toInt())
            
            // 2. Firestore
            try {
                if (currentUserId != 0) {
                    firestoreRepo.createConversation(currentUserId, updatedChatItem)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun seedConversations() {
        val items = listOf(
            ChatItem(1, "Lucas Bernard", "Tu as fini le rapport de TP pour demain ?", "14:20", unreadCount = 2, hasOnlineStatus = true),
            ChatItem(2, "Sarah Martin", "On se capte à la cafétéria à 12h30 ?", "Hier"),
            ChatItem(3, "BDE UTBM – Gala 2024", "La vente des billets est officiellement ouverte !", "Mar", isGroup = true),
            ChatItem(4, "Thomas Dupont (Assistance)", "Message vocal (0:42)", "Lun", messageType = MessageType.VOICE_MESSAGE),
            ChatItem(5, "Emma Wilson", "A envoyé une photo", "10:15", messageType = MessageType.IMAGE),
            ChatItem(6, "Cécile Dubois", "Le cours de SY43 est déplacé en amphi Nord.", "Ven", isCourse = true),
            ChatItem(7, "Projet Mobile SY43", "Est-ce qu'on ajoute Firebase ?", "11:05", isGroup = true)
        )
        items.forEach { repo.insertConversation(it) }

        listOf(
            Message(chatItemId = 1, text = "Salut ! Tu as pu avancer sur le projet SY43 ?", isFromUser = false, time = "10:00"),
            Message(chatItemId = 1, text = "Oui, j'ai fini la partie UI du chat.", isFromUser = true, time = "10:05"),
            Message(chatItemId = 1, text = "Top ! Je m'occupe de la base de données alors.", isFromUser = false, time = "10:06")
        ).forEach { repo.insertMessage(it) }
    }
}
