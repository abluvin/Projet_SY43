package com.example.projet.data

data class CommentWithAuthor(
    val id: Int,
    val postId: Int,
    val content: String,
    val timestamp: Long,
    val userId: Int,
    val authorName: String
)
