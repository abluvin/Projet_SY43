package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_member",
    foreignKeys = [
        ForeignKey(entity = ChatItem::class, parentColumns = ["id"], childColumns = ["chatItemId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("chatItemId"),
        Index("userId"),
        Index(value = ["chatItemId", "userId"], unique = true)
    ]
)
data class ChatMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatItemId: Int,
    val userId: Int
)
