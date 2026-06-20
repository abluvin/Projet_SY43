package com.example.projet.data.firebase

import com.example.projet.data.Post
import com.example.projet.data.Poll

class PostFireStoreRepository(
    private val firestore: FireStoreRepository
) {
    private val collection = "posts"
    private val pollCollection = "polls"

    suspend fun createPost(post: Post) {
        firestore.add(collection, post.id.toString(), post)
    }

    suspend fun createPoll(poll: Poll) {
        firestore.add(pollCollection, poll.id.toString(), poll)
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
