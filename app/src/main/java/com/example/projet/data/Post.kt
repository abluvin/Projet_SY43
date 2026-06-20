package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "post",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["idUser"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("idUser")]
)
data class Post(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var text: String = "",
    var idUser: Int,
    var imageUrl: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var ue: String? = null,
    var voiceFilePath: String? = null,
    var voiceDuration: Long = 0L,
    var isPoll: Boolean = false,
    var branch: String? = null
)