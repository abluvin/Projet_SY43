package com.example.projet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_ue",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UE::class, parentColumns = ["id"], childColumns = ["ueId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("userId"),
        Index("ueId"),
        Index(value = ["userId", "ueId"], unique = true)
    ]
)
data class UserUE(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val ueId: Int,
    val enseigne: Boolean = false
)
