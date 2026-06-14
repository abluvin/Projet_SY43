package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poll_vote",
    foreignKeys = [
        ForeignKey(
            entity = PollOption::class,
            parentColumns = ["id"],
            childColumns = ["optionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("optionId"), Index("userId")]
)
data class PollVote(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var optionId: Int,
    var userId: Int,
    var timestamp: Long = System.currentTimeMillis()
)

