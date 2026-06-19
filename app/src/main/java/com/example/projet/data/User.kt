package com.example.projet.data
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole { STUDENT, PROFESSOR }

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var isAdmin: Boolean = false,
    var role: String = UserRole.STUDENT.name,
    var branch: String = ""
)