package com.example.projet.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.ChatItem
import com.example.projet.data.ChatPoll
import com.example.projet.data.ChatPollOption
import com.example.projet.data.ChatPollVote
import com.example.projet.data.Message
import com.example.projet.data.MessageType
import com.example.projet.data.firebase.ChatFireStoreRepository
import com.example.projet.data.firebase.FireStoreRepository
import com.example.projet.data.UE
import com.example.projet.data.User
import com.example.projet.data.UserUEWithDetails
import com.example.projet.data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepo = ChatFireStoreRepository(FireStoreRepository())
    private val db = (application as ProjetApplication).database
    private val repo: ChatRepository = ChatRepository(
        db.chatItemDao(),
        db.messageDao(),
        db.chatPollDao(),
        db.chatPollOptionDao(),
        db.chatPollVoteDao(),
        db.chatMemberDao()
    )
    private val userDao = db.userDao()
    private val userUEDao = db.userUEDao()
    private val ueDao = db.ueDao()

    private val _currentUserId = MutableStateFlow(0)
    val currentUserId: StateFlow<Int> = _currentUserId

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversations: StateFlow<List<ChatItem>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid == 0) flowOf(emptyList())
            else repo.getConversationsForUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = userDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUEs: StateFlow<List<UE>> = ueDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val profUEs: StateFlow<List<UserUEWithDetails>> = _currentUserId
        .flatMapLatest { uid ->
            if (uid == 0) flowOf(emptyList())
            else userUEDao.getByUserWithDetails(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (repo.getAllConversations().first().isEmpty()) seedConversations()
        }
    }

    fun initUser(userId: Int) {
        _currentUserId.value = userId
    }

    fun getDirectChatName(chatItemId: Int): Flow<String?> =
        repo.getOtherMemberName(chatItemId, _currentUserId.value)

    fun getMessages(chatItemId: Int): Flow<List<Message>> = repo.getMessages(chatItemId)

    fun getChatItem(id: Int): Flow<ChatItem?> = repo.getChatItem(id)

    fun getLastMessage(chatItemId: Int): Flow<String?> = repo.getLastMessage(chatItemId)

    fun getPollOptions(pollId: Int): Flow<List<ChatPollOption>> = repo.getPollOptions(pollId)

    fun getUserVotesForPoll(userId: Int, pollId: Int): Flow<List<ChatPollVote>> =
        repo.getUserVotesForPoll(userId, pollId)

    fun sendMessage(chatItemId: Int, text: String) {
        viewModelScope.launch {
            val time = now()
            val message = Message(chatItemId = chatItemId, text = text, senderId = _currentUserId.value, time = time)
            val id = repo.insertMessage(message)
            try {
                firestoreRepo.sendMessage(message.copy(id = id.toInt()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendAnnouncement(chatItemId: Int, text: String) {
        viewModelScope.launch {
            val time = now()
            val message = Message(chatItemId = chatItemId, text = text, senderId = _currentUserId.value, time = time, isAnnouncement = true)
            val id = repo.insertMessage(message)
            try {
                firestoreRepo.sendMessage(message.copy(id = id.toInt()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendAudioMessage(chatItemId: Int, audioPath: String, durationMs: Long) {
        viewModelScope.launch {
            val time = now()
            repo.insertMessage(
                Message(
                    chatItemId = chatItemId,
                    text = "Message vocal",
                    senderId = _currentUserId.value,
                    time = time,
                    messageType = MessageType.VOICE_MESSAGE,
                    audioPath = audioPath,
                    audioDuration = durationMs
                )
            )
        }
    }

    fun createPoll(chatItemId: Int, question: String, options: List<String>) {
        viewModelScope.launch {
            val pollId = repo.insertPoll(
                ChatPoll(
                    chatItemId = chatItemId,
                    question = question,
                    creatorId = _currentUserId.value
                )
            )
            options.forEach { optText ->
                repo.insertPollOption(ChatPollOption(pollId = pollId.toInt(), text = optText))
            }
            val time = now()
            repo.insertMessage(
                Message(
                    chatItemId = chatItemId,
                    text = question,
                    senderId = _currentUserId.value,
                    time = time,
                    messageType = MessageType.POLL,
                    pollId = pollId.toInt()
                )
            )
        }
    }

    fun voteForOption(optionId: Int) {
        viewModelScope.launch {
            repo.addVote(optionId, _currentUserId.value)
        }
    }

    fun createCourseHub(name: String, profName: String, ueCode: String? = null) {
        viewModelScope.launch {
            val time = now()
            repo.insertConversation(
                ChatItem(
                    id = 0,
                    name = name,
                    lastMessage = "Hub créé par $profName",
                    time = time,
                    isCourse = true,
                    ueCode = ueCode
                )
            )
        }
    }

    fun createDirectConversation(contactName: String, memberIds: List<Int>, onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val time = now()
            val id = repo.insertConversation(
                ChatItem(
                    id = 0,
                    name = contactName,
                    lastMessage = "",
                    time = time,
                    isGroup = false
                )
            )
            memberIds.forEach { repo.addMember(id.toInt(), it) }
            onCreated(id.toInt())
        }
    }

    fun createGroupConversation(members: List<String>, memberIds: List<Int>, onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val time = now()
            val name = members.joinToString(", ")
            val id = repo.insertConversation(
                ChatItem(
                    id = 0,
                    name = name,
                    lastMessage = "",
                    time = time,
                    isGroup = true
                )
            )
            memberIds.forEach { repo.addMember(id.toInt(), it) }
            onCreated(id.toInt())
        }
    }

    private fun now(): String =
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    private suspend fun seedConversations() {
        val items = listOf(
            ChatItem(1, "Lucas Bernard", "Tu as fini le rapport de TP pour demain ?", "14:20", unreadCount = 2, hasOnlineStatus = true),
            ChatItem(2, "Sarah Martin", "On se capte à la cafétéria à 12h30 ?", "Hier"),
            ChatItem(3, "BDE UTBM – Gala 2024", "La vente des billets est officiellement ouverte !", "Mar", isGroup = true),
            ChatItem(4, "Thomas Dupont (Assistance)", "Message vocal (0:42)", "Lun", messageType = MessageType.VOICE_MESSAGE),
            ChatItem(5, "Emma Wilson", "A envoyé une photo", "10:15", messageType = MessageType.IMAGE),
            ChatItem(6, "SY43", "Le cours est déplacé en amphi Nord.", "Ven", isCourse = true, ueCode = "SY43"),
            ChatItem(7, "Projet Mobile SY43", "Est-ce qu'on ajoute Firebase ?", "11:05", isGroup = true)
        )
        items.forEach { repo.insertConversation(it) }

        listOf(
            Message(chatItemId = 1, text = "Salut ! Tu as pu avancer sur le projet SY43 ?", senderId = 0, time = "10:00"),
            Message(chatItemId = 1, text = "Oui, j'ai fini la partie UI du chat.", senderId = 0, time = "10:05"),
            Message(chatItemId = 1, text = "Top ! Je m'occupe de la base de données alors.", senderId = 0, time = "10:06")
        ).forEach { repo.insertMessage(it) }
    }
}
