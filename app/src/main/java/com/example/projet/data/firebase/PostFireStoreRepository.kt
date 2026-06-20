package com.example.projet.data.firebase

import com.example.projet.data.Post

class PostFireStoreRepository(
    private val firestore: FireStoreRepository
) {
    private val collection = "posts"

    suspend fun createPost(post: Post) {
        firestore.add(collection, post.id.toString(), post)
    }

    suspend fun getPosts(): List<Post> {
        return firestore.getAll(collection, Post::class.java)
    }

    suspend fun getPost(id: String): Post? {
        return firestore.getById(collection, id, Post::class.java)
    }

    suspend fun deletePost(id: String) {
        firestore.delete(collection, id)
    }
}
