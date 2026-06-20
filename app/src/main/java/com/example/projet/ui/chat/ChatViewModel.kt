package com.example.projet.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.ChatItem
import com.example.projet.data.Message
import com.example.projet.data.MessageType
import com.example.projet.data.User
import com.example.projet.data.repository.ChatRepository
import com.example.projet.data.repository.UserRepository
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
    private val userRepo: UserRepository

    init {
        val db = (application as ProjetApplication).database
        repo = ChatRepository(db.chatItemDao(), db.messageDao())
        userRepo = UserRepository(db.userDao())
        viewModelScope.launch {
            if (repo.getAllConversations().first().isEmpty()) seedConversations()
        }
    }

    val conversations: StateFlow<List<ChatItem>> = repo.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = userRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMessages(chatItemId: Int): Flow<List<Message>> = repo.getMessages(chatItemId)

    fun getChatItem(id: Int): Flow<ChatItem?> = repo.getChatItem(id)

    fun getLastMessage(chatItemId: Int): Flow<String?> = repo.getLastMessage(chatItemId)

    fun sendMessage(chatItemId: Int, text: String) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repo.insertMessage(Message(chatItemId = chatItemId, text = text, isFromUser = true, time = time, type = MessageType.TEXT))
            repo.updateLastMessage(chatItemId, text, time)
        }
    }

    fun sendVoiceMessage(chatItemId: Int, filePath: String, durationMs: Long) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repo.insertMessage(
                Message(
                    chatItemId = chatItemId,
                    isFromUser = true,
                    time = time,
                    type = MessageType.VOICE_MESSAGE,
                    voiceFilePath = filePath,
                    voiceDuration = durationMs
                )
            )
            repo.updateLastMessage(chatItemId, "🎤 Message vocal", time)
        }
    }

    fun sendAnnouncement(chatItemId: Int, text: String) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repo.insertMessage(Message(chatItemId = chatItemId, text = text, isFromUser = true, time = time, isAnnouncement = true, type = MessageType.TEXT))
            repo.updateLastMessage(chatItemId, "📢 $text", time)
        }
    }

    fun sendPoll(chatItemId: Int, question: String, options: List<String>) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repo.insertMessage(
                Message(
                    chatItemId = chatItemId,
                    isFromUser = true,
                    time = time,
                    type = MessageType.POLL,
                    pollQuestion = question,
                    pollOptions = options,
                    pollVotes = options.indices.associateWith { 0 }
                )
            )
            repo.updateLastMessage(chatItemId, "📊 Sondage : $question", time)
        }
    }

    fun votePoll(messageId: Int, optionIndex: Int) {
        viewModelScope.launch {
            val message = repo.getMessageById(messageId)
            if (message != null && message.type == MessageType.POLL) {
                val currentVotes = message.pollVotes?.toMutableMap() ?: mutableMapOf()
                val count = currentVotes[optionIndex] ?: 0
                currentVotes[optionIndex] = count + 1
                repo.updateMessage(message.copy(pollVotes = currentVotes))
            }
        }
    }

    fun markAsRead(chatItemId: Int) {
        viewModelScope.launch {
            repo.markAsRead(chatItemId)
        }
    }

    fun startOrGetConversation(recipientNames: List<String>, onResult: (ChatItem) -> Unit) {
        viewModelScope.launch {
            if (recipientNames.size == 1) {
                val name = recipientNames[0]
                val existing = repo.getConversationByName(name)
                if (existing != null) {
                    onResult(existing)
                } else {
                    val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    val newItem = ChatItem(id = 0, name = name, lastMessage = "Nouvelle discussion", time = time)
                    val id = repo.insertConversation(newItem).toInt()
                    onResult(newItem.copy(id = id))
                }
            } else {
                // Create group
                val name = recipientNames.joinToString(", ")
                val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                val newItem = ChatItem(id = 0, name = name, lastMessage = "Groupe créé", time = time, isGroup = true)
                val id = repo.insertConversation(newItem).toInt()
                onResult(newItem.copy(id = id))
            }
        }
    }

    fun createCourseHub(name: String, profName: String) {
        viewModelScope.launch {
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            repo.insertConversation(
                ChatItem(
                    id = 0,
                    name = name,
                    lastMessage = "Hub créé par $profName",
                    time = time,
                    isCourse = true
                )
            )
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
        
        // Update previews for seeded data
        repo.updateLastMessage(1, "Top ! Je m'occupe de la base de données alors.", "10:06")
    }
}
