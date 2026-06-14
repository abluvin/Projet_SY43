package com.example.projet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_item")
data class ChatItem(
    @PrimaryKey(autoGenerate = true)
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
