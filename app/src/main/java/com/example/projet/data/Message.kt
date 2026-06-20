package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message",
    foreignKeys = [ForeignKey(
        entity = ChatItem::class,
        parentColumns = ["id"],
        childColumns = ["chatItemId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("chatItemId")]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val chatItemId: Int = 0,
    val text: String,
    val senderId: Int = 0,
    val time: String,
    val isAnnouncement: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val audioPath: String? = null,
    val audioDuration: Long = 0,
    val pollId: Int? = null
)
