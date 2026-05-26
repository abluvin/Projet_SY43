package com.example.projet.data

data class CourseMaterial(
    val title: String,
    val description: String,
    val date: String,
    val type: String = "PDF"
)