package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poll",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postId"), Index("creatorId")]
)
data class Poll(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var postId: Int,
    var creatorId: Int,
    var question: String,
    var timestamp: Long = System.currentTimeMillis()
)

