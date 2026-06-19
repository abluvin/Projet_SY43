package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "voice_message",
    foreignKeys = [
        ForeignKey(entity = Post::class, parentColumns = ["id"], childColumns = ["postId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("postId"), Index("userId")]
)
data class VoiceMessage(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var postId: Int,
    var userId: Int,
    var filePath: String,
    var duration: Long = 0L,
    var timestamp: Long = System.currentTimeMillis()
)
