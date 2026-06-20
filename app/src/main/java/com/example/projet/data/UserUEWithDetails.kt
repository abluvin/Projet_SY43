package com.example.projet.data

import androidx.room.Embedded
import androidx.room.Relation

data class UserUEWithDetails(
    @Embedded val userUE: UserUE,
    @Relation(parentColumn = "ueId", entityColumn = "id")
    val ue: UE
)
