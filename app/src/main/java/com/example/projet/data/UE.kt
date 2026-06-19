package com.example.projet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ue")
data class UE(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val name: String,
    val branch: String = "Transversal"
)
