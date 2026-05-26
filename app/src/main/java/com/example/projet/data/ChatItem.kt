package com.example.projet.data
data class ChatItem(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val isCourse: Boolean = false,
    val hasOnlineStatus: Boolean = false,
    val messageType: MessageType = MessageType.TEXT
)
