package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_poll")
data class ChatPoll(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatItemId: Int,
    val question: String,
    val creatorId: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_poll_option",
    foreignKeys = [ForeignKey(
        entity = ChatPoll::class,
        parentColumns = ["id"],
        childColumns = ["pollId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("pollId")]
)
data class ChatPollOption(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pollId: Int,
    val text: String,
    val voteCount: Int = 0
)

@Entity(
    tableName = "chat_poll_vote",
    foreignKeys = [ForeignKey(
        entity = ChatPollOption::class,
        parentColumns = ["id"],
        childColumns = ["optionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("optionId")]
)
data class ChatPollVote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val optionId: Int,
    val userId: Int
)
