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
    val text: String = "",
    val isFromUser: Boolean,
    val time: String,
    val isAnnouncement: Boolean = false,
    val type: MessageType = MessageType.TEXT,
    val voiceFilePath: String? = null,
    val voiceDuration: Long = 0L,
    val pollQuestion: String? = null,
    val pollOptions: List<String>? = null,
    val pollVotes: Map<Int, Int>? = null // OptionIndex to VoteCount
)
