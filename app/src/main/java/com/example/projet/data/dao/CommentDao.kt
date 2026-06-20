package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Comment
import com.example.projet.data.CommentWithAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Query("SELECT * FROM comment WHERE postId = :postId ORDER BY timestamp ASC")
    fun getByPost(postId: Int): Flow<List<Comment>>

    @Query("""
        SELECT c.id, c.postId, c.content, c.timestamp, c.userId, u.name as authorName
        FROM comment c
        INNER JOIN user u ON c.userId = u.id
        WHERE c.postId = :postId
        ORDER BY c.timestamp ASC
    """)
    fun getByPostWithAuthors(postId: Int): Flow<List<CommentWithAuthor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: Comment): Long

    @Delete
    suspend fun delete(comment: Comment)

    @Query("DELETE FROM comment WHERE postId = :postId")
    suspend fun deleteByPost(postId: Int)
}
