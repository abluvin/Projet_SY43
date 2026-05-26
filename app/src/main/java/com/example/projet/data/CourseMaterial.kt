package com.example.projet.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_material")
data class CourseMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val date: String,
    val type: String = "PDF"
)