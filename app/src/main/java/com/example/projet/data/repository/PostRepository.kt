package com.example.projet.data.repository

import com.example.projet.data.Comment
import com.example.projet.data.Post
import com.example.projet.data.dao.CommentDao
import com.example.projet.data.dao.PostDao
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao
) {

    fun getAll(): Flow<List<Post>> = postDao.getAll()

    fun getByUser(userId: Int): Flow<List<Post>> = postDao.getByUser(userId)

    suspend fun insert(post: Post): Long = postDao.insert(post)

    suspend fun delete(post: Post) = postDao.delete(post)

    fun getComments(postId: Int): Flow<List<Comment>> = commentDao.getByPost(postId)

    suspend fun insertComment(comment: Comment): Long = commentDao.insert(comment)

    suspend fun deleteComment(comment: Comment) = commentDao.delete(comment)
}
