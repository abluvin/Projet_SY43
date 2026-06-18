package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poll_option",
    foreignKeys = [ForeignKey(entity = Poll::class, parentColumns = ["id"], childColumns = ["pollId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("pollId")]
)
data class PollOption(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var pollId: Int,
    var text: String,
    var voteCount: Int = 0
)
